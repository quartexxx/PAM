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
            .fallbackToDestructiveMigration()
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
                val byePlayer = Player(name = "Wolny Los", isBye = true)
                val selectedSystem = remember { mutableStateOf("Szwajcarski") } // Przechowywanie wybranego systemu
                val navController = rememberNavController()
                val selectedTieBreak = remember { mutableStateOf("Progres") } // Domyślna metoda remisów

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

                            composable("configure_tournament") {
                                ConfigureTournamentScreen(
                                    selectedPlayers = selectedPlayers,
                                    onStartTournament = { system, _, _ ->
                                        selectedSystem.value = system // Ustawienie wybranego systemu
                                        lifecycleScope.launch {
                                            selectedPairs.clear()
                                            matchResults.clear()
                                            playedPairs.clear()

                                            totalRounds.value = if (system == "Szwajcarski") {
                                                ceil(log2(selectedPlayers.size.toDouble())).toInt()
                                            } else {
                                                ceil(log2(selectedPlayers.size.toDouble())).toInt()
                                            }

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

                            composable("round_screen/{roundNumber}") { backStackEntry ->
                                val roundNumber = backStackEntry.arguments?.getString("roundNumber")?.toInt() ?: 1
                                RoundScreen(
                                    roundNumber = roundNumber,
                                    pairs = selectedPairs,
                                    system = selectedSystem.value,
                                    onRoundComplete = { results ->
                                        matchResults.addAll(results)

                                        if (roundNumber < totalRounds.value) {
                                            val updatedPairs = generateNextRoundPairs(
                                                results, playedPairs, byePlayer, selectedSystem.value
                                            )
                                            selectedPairs.clear()
                                            selectedPairs.addAll(updatedPairs)

                                            navController.navigate("round_screen/${roundNumber + 1}")
                                        } else {
                                            navController.navigate("tournament_results")
                                        }
                                    },
                                    onBackToSettings = {
                                        navController.navigate("configure_tournament")
                                    }
                                )
                            }

                            composable("tournament_results") {
                                val finalResults: Map<Player, Float> = calculateTieBreakResults(
                                    matchResults = matchResults,
                                    tieBreak = selectedTieBreak.value
                                )

                                TournamentResultsScreen(
                                    results = finalResults,
                                    system = selectedSystem.value, // Przekazujemy wybrany system
                                    onRestartTournament = {
                                        lifecycleScope.launch {
                                            selectedPlayers.clear()
                                            selectedPairs.clear()
                                            matchResults.clear()
                                            playedPairs.clear()
                                            totalRounds.value = 0
                                            navController.navigate("add_players") {
                                                popUpTo(0)
                                            }
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
        results: List<Pair<Pair<Player, Player>, Pair<Float, Float>>>,
        playedPairs: MutableSet<Pair<Player, Player>>,
        byePlayer: Player,
        system: String
    ): List<Pair<Player, Player>> {
        val pairs = mutableListOf<Pair<Player, Player>>()

        if (system == "Szwajcarski") {
            val updatedPlayers = results.flatMap { (pair, scores) ->
                listOf(pair.first to scores.first, pair.second to scores.second)
            }
                .groupBy({ it.first }, { it.second })
                .map { (player, scores) -> player to scores.sum() }
                .sortedByDescending { it.second }

            val remainingPlayers = updatedPlayers.map { it.first }.toMutableList()

            if (remainingPlayers.size % 2 != 0) {
                remainingPlayers.add(byePlayer)
            }

            while (remainingPlayers.size > 1) {
                val player1 = remainingPlayers.removeAt(0)
                val opponent = remainingPlayers.firstOrNull { player2 ->
                    val newPair = if (player1.name < player2.name) Pair(player1, player2) else Pair(player2, player1)
                    newPair !in playedPairs
                }

                if (opponent != null) {
                    remainingPlayers.remove(opponent)
                    val newPair = if (player1.name < opponent.name) Pair(player1, opponent) else Pair(opponent, player1)
                    pairs.add(newPair)
                    playedPairs.add(newPair)
                } else {
                    remainingPlayers.add(player1)
                }
            }
        } else if (system == "Pucharowy") {
            val qualifiedPlayers = results.filter { it.second.first > it.second.second }
                .map { it.first.first } + results.filter { it.second.second > it.second.first }
                .map { it.first.second }

            val remainingPlayers = qualifiedPlayers.toMutableList()
            if (remainingPlayers.size % 2 != 0) {
                remainingPlayers.add(byePlayer)
            }

            while (remainingPlayers.size > 1) {
                val player1 = remainingPlayers.removeAt(0)
                val player2 = remainingPlayers.removeAt(0)
                pairs.add(Pair(player1, player2))
            }
        }

        return pairs
    }

    private fun calculateFinalResults(
        matchResults: List<Pair<Pair<Player, Player>, Pair<Float, Float>>>,
        system: String
    ): Map<Player, Float> {
        return if (system == "Szwajcarski") {
            matchResults.flatMap { (pair, scores) ->
                listOf(pair.first to scores.first, pair.second to scores.second)
            }
                .groupBy({ it.first }, { it.second })
                .mapValues { (_, scores) -> scores.sum() }
        } else {
            // Tylko zwycięzca
            val winner = matchResults.lastOrNull()?.let {
                if (it.second.first > it.second.second) it.first.first else it.first.second
            }
            if (winner != null) mapOf(winner to 1f) else emptyMap()
        }
    }
    private fun calculateTieBreakResults(
        matchResults: List<Pair<Pair<Player, Player>, Pair<Float, Float>>>,
        tieBreak: String
    ): Map<Player, Float> {
        return when (tieBreak) {
            "Progres" -> calculateProgress(matchResults)
            "Sumaryczny" -> calculateSumaryczny(matchResults)
            else -> emptyMap()
        }
    }

    // System Progres (już zaimplementowany)
    private fun calculateProgress(
        matchResults: List<Pair<Pair<Player, Player>, Pair<Float, Float>>>
    ): Map<Player, Float> {
        val cumulativeScores = mutableMapOf<Player, Float>()
        val progressScores = mutableMapOf<Player, Float>()

        matchResults.forEach { (pair, scores) ->
            cumulativeScores[pair.first] = (cumulativeScores[pair.first] ?: 0f) + scores.first
            cumulativeScores[pair.second] = (cumulativeScores[pair.second] ?: 0f) + scores.second
            progressScores[pair.first] = (progressScores[pair.first] ?: 0f) + cumulativeScores[pair.first]!!
            progressScores[pair.second] = (progressScores[pair.second] ?: 0f) + cumulativeScores[pair.second]!!
        }

        return progressScores
    }

    // System Sumaryczny
    private fun calculateSumaryczny(
        matchResults: List<Pair<Pair<Player, Player>, Pair<Float, Float>>>
    ): Map<Player, Float> {
        return matchResults.flatMap { (pair, scores) ->
            listOf(pair.first to scores.first, pair.second to scores.second)
        }.groupBy({ it.first }, { it.second })
            .mapValues { (_, scores) -> scores.sum() }
    }

}
