package com.example.chesstournamentmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.chesstournamentmanager.data.AppDatabase
import com.example.chesstournamentmanager.data.Player
import com.example.chesstournamentmanager.ui.theme.ChessTournamentManagerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "chess-database"
        ).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChessTournamentManagerTheme {
                val players = remember { mutableStateListOf<Player>() }
                val selectedPlayers = remember { mutableStateListOf<Player>() }
                val navController = rememberNavController()

                // Pobierz zawodników z bazy przy starcie w LaunchedEffect
                LaunchedEffect(Unit) {
                    val allPlayers = db.playerDao().getAllPlayers()
                    players.clear()
                    players.addAll(allPlayers)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    content = { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "add_players"
                        ) {
                            // Ekran dodawania zawodników
                            composable("add_players") {
                                AddPlayerScreen(
                                    players = players,
                                    onAddPlayer = { name, rating ->
                                        lifecycleScope.launch {
                                            val player = Player(name = name, rating = rating)
                                            db.playerDao().insert(player)
                                            val allPlayers = db.playerDao().getAllPlayers()
                                            players.clear()
                                            players.addAll(allPlayers)
                                        }
                                    },
                                    onClearPlayers = {
                                        lifecycleScope.launch {
                                            db.playerDao().deleteAllPlayers()
                                            players.clear()
                                        }
                                    },
                                    onNavigateToSelectPlayers = {
                                        navController.navigate("select_players")
                                    }
                                )
                            }

                            // Ekran wyboru zawodników
                            composable("select_players") {
                                SelectPlayersScreen(
                                    players = players,
                                    onPlayerSelected = { player, isSelected ->
                                        if (isSelected) {
                                            selectedPlayers.add(player)
                                        } else {
                                            selectedPlayers.remove(player)
                                        }
                                    },
                                    onProceed = {
                                        navController.navigate("configure_tournament") // Nawigacja do konfiguracji turnieju
                                    },
                                    onBackToAddPlayers = {
                                        navController.navigate("add_players") // Nawigacja do ekranu dodawania zawodników
                                    },
                                    navController = navController // Przekazanie navController
                                )
                            }


                            // Ekran konfiguracji turnieju
                            composable("configure_tournament") {
                                ConfigureTournamentScreen(
                                    selectedPlayers = selectedPlayers,
                                    onStartTournament = { system, rounds, tieBreak ->
                                        println("System: $system, Liczba rund: $rounds, TieBreak: $tieBreak")
                                        // Tutaj możesz dodać logikę rozpoczynania turnieju
                                    },
                                    onBackToSelectPlayers = {
                                        navController.navigate("select_players")
                                    }
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}
