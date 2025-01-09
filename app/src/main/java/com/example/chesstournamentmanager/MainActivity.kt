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
import kotlin.math.ceil
import kotlin.math.log2

class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "chess-database"
        )
            .fallbackToDestructiveMigration() // Użyj destrukcyjnej migracji
            .build()
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
                val matchResults = remember { mutableStateListOf<Pair<Pair<Player, Player>, Pair<Float, Float>>>() }
                val playedPairs = remember { mutableSetOf<Pair<Player, Player>>() }
                val byePlayer = Player(name = "Wolny Los", isBye = true) // Definiowanie "Wolnego Losu"
                val navController = rememberNavController()

                // Pobierz zawodników z bazy przy starcie
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
                            startDestination = "add_players",
                            modifier = Modifier.padding(innerPadding)
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
                                    onStartTournament = { system, _, _ ->
                                        lifecycleScope.launch {
                                            selectedPairs.clear()
                                            matchResults.clear()
                                            playedPairs.clear()

                                            // System szwajcarski: liczba rund zależna od liczby graczy
                                            if (system == "Szwajcarski") {
                                                totalRounds.value = ceil(log2(selectedPlayers.size.toDouble())).toInt()
                                            }

                                            // Dodaj "Wolnego Losa", jeśli liczba graczy jest nieparzysta
                                            val playersForPairing = selectedPlayers.toMutableList()
                                            if (playersForPairing.size % 2 != 0) {
                                                playersForPairing.add(byePlayer)
                                            }

                                            val sortedPlayers = playersForPairing.sortedBy { it.rating ?: 0 }
                                            for (i in sortedPlayers.indices step 2) {
                                                if (i + 1 < sortedPlayers.size) {
                                                    val pair = Pair(sortedPlayers[i], sortedPlayers[i + 1])
                                                    selectedPairs.add(pair)
                                                    playedPairs.add(pair)
                                                    playedPairs.add(Pair(sortedPlayers[i + 1], sortedPlayers[i]))
                                                }
                                            }
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
                                        // Dodaj wyniki rundy do wyników turnieju
                                        matchResults.addAll(results)

                                        // Sprawdź, czy to ostatnia runda
                                        if (roundNumber < totalRounds.value) {
                                            // Generuj nowe pary
                                            val updatedPairs = generateNextRoundPairs(results, playedPairs, byePlayer)
                                            selectedPairs.clear()
                                            selectedPairs.addAll(updatedPairs)

                                            // Nawiguj do kolejnej rundy
                                            navController.navigate("round_screen/${roundNumber + 1}")
                                        } else {
                                            // Przejdź do wyników końcowych
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
                                val finalResults: Map<Player, Float> = matchResults
                                    .flatMap { (pair, scores) ->
                                        listOf(
                                            pair.first to scores.first,
                                            pair.second to scores.second
                                        )
                                    }
                                    .groupBy({ it.first }, { it.second })
                                    .mapValues { (_, scores) -> scores.sum() }

                                TournamentResultsScreen(
                                    results = finalResults,
                                    onRestartTournament = {
                                        selectedPlayers.clear()
                                        selectedPairs.clear()
                                        matchResults.clear()
                                        playedPairs.clear()
                                        totalRounds.value = 0
                                        navController.popBackStack("add_players", inclusive = true)
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
        results: List<Pair<Pair<Player, Player>, Pair<Float, Float>>>,
        playedPairs: MutableSet<Pair<Player, Player>>,
        byePlayer: Player
    ): List<Pair<Player, Player>> {
        val updatedPlayers = results.flatMap { (pair, scores) ->
            listOf(
                pair.first to scores.first,
                pair.second to scores.second
            )
        }.groupBy({ it.first }, { it.second })
            .map { (player, scores) -> player to scores.sum() }
            .sortedByDescending { it.second } // Sortuj graczy według punktów

        val pairs = mutableListOf<Pair<Player, Player>>()

        // Twórz nowe pary, unikając rozegranych par
        val remainingPlayers = updatedPlayers.map { it.first }.toMutableList()
        if (remainingPlayers.size % 2 != 0) {
            remainingPlayers.add(byePlayer)
        }

        while (remainingPlayers.size > 1) {
            val player1 = remainingPlayers.removeAt(0)
            val opponent = remainingPlayers.firstOrNull { player2 ->
                val newPair = Pair(player1, player2).sorted()
                newPair !in playedPairs
            }

            if (opponent != null) {
                remainingPlayers.remove(opponent)
                val newPair = Pair(player1, opponent).sorted()
                pairs.add(newPair)
                playedPairs.add(newPair)
            } else {
                remainingPlayers.add(player1) // Jeśli nie można znaleźć przeciwnika, dodaj gracza na koniec kolejki
            }
        }

        return pairs
    }

    private fun Pair<Player, Player>.sorted(): Pair<Player, Player> {
        return if (first.name < second.name) this else Pair(second, first)
    }
}
