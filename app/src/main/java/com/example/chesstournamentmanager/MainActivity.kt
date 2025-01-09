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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.chesstournamentmanager.data.AppDatabase
import com.example.chesstournamentmanager.data.Player
import com.example.chesstournamentmanager.ui.TournamentResultsScreen
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
                val selectedPairs = remember { mutableStateListOf<Pair<Player, Player>>() }
                val totalRounds = remember { mutableStateOf(0) }
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
                                        // Przejście do konfiguracji turnieju
                                        navController.navigate("configure_tournament")
                                    },
                                    onBackToAddPlayers = {
                                        navController.navigate("add_players")
                                    },
                                    navController = navController
                                )
                            }

                            // Ekran konfiguracji turnieju
                            composable("configure_tournament") {
                                ConfigureTournamentScreen(
                                    selectedPlayers = selectedPlayers,
                                    onStartTournament = { system, rounds, tieBreak ->
                                        lifecycleScope.launch {
                                            selectedPairs.clear()
                                            totalRounds.value = rounds

                                            // Generowanie par zawodników (np. system szwajcarski)
                                            val sortedPlayers = selectedPlayers.sortedBy { it.rating ?: 0 }
                                            for (i in sortedPlayers.indices step 2) {
                                                if (i + 1 < sortedPlayers.size) {
                                                    selectedPairs.add(Pair(sortedPlayers[i], sortedPlayers[i + 1]))
                                                }
                                            }

                                            // Nawigacja do pierwszej rundy
                                            navController.navigate("round_screen/1")
                                        }
                                    },
                                    onBackToSelectPlayers = {
                                        navController.navigate("select_players")
                                    }
                                )
                            }

                            // Ekran rundy
                            composable("round_screen/{roundNumber}") { backStackEntry ->
                                val roundNumber = backStackEntry.arguments?.getString("roundNumber")?.toInt() ?: 1
                                RoundScreen(
                                    roundNumber = roundNumber,
                                    pairs = selectedPairs,
                                    onRoundComplete = { results ->
                                        println("Wyniki rundy $roundNumber: $results")

                                        // Logika przejścia do następnej rundy lub zakończenia turnieju
                                        if (roundNumber < totalRounds.value) {
                                            val updatedPairs = generateNextRoundPairs(results)
                                            selectedPairs.clear()
                                            selectedPairs.addAll(updatedPairs)

                                            navController.navigate("round_screen/${roundNumber + 1}")
                                        } else {
                                            println("Turniej zakończony!")
                                            navController.navigate("tournament_results")
                                        }
                                    },
                                    onBackToSettings = {
                                        navController.navigate("configure_tournament")
                                    }
                                )
                            }

                            // Ekran wyników końcowych
                            composable("tournament_results") {
                                TournamentResultsScreen(
                                    players = selectedPlayers,
                                    onBackToHome = {
                                        navController.navigate("add_players") {
                                            popUpTo("add_players") { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
                    }
                )
            }
        }
    }

    private fun generateNextRoundPairs(
        results: List<Pair<Pair<Player, Player>, Pair<Int, Int>>>
    ): List<Pair<Player, Player>> {
        val updatedPlayers = results.flatMap { (pair, scores) ->
            listOf(
                pair.first to scores.first,
                pair.second to scores.second
            )
        }.groupBy({ it.first }, { it.second })
            .map { (player, scores) -> player to scores.sum() }
            .sortedByDescending { it.second }

        val pairs = mutableListOf<Pair<Player, Player>>()
        for (i in updatedPlayers.indices step 2) {
            if (i + 1 < updatedPlayers.size) {
                pairs.add(Pair(updatedPlayers[i].first, updatedPlayers[i + 1].first))
            }
        }
        return pairs
    }
}
