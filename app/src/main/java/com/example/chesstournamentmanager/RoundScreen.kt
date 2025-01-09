package com.example.chesstournamentmanager

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chesstournamentmanager.data.Player

@Composable
fun RoundScreen(
    roundNumber: Int,
    pairs: List<Pair<Player, Player>>,
    onRoundComplete: (List<Pair<Pair<Player, Player>, Pair<Int, Int>>>) -> Unit,
    onBackToSettings: () -> Unit
) {
    // Przechowywanie wyników każdej pary
    var results by remember {
        mutableStateOf(pairs.map { it to (0 to 0) }) // Typ danych to Pair<Pair<Player, Player>, Pair<Int, Int>>
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Runda $roundNumber",
            style = MaterialTheme.typography.headlineSmall
        )

        // Wyświetlanie par zawodników z polami na wyniki
        results.forEachIndexed { index, pairResult ->
            val (player1, player2) = pairResult.first
            val (score1, score2) = pairResult.second

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Zawodnik 1
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = player1.name)
                    TextField(
                        value = score1.toString(),
                        onValueChange = { score ->
                            results = results.toMutableList().apply {
                                val newScore = score.toIntOrNull() ?: 0
                                this[index] = this[index].first to (newScore to this[index].second.second)
                            }
                        },
                        modifier = Modifier.width(60.dp)
                    )
                }

                Text("vs")

                // Zawodnik 2
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = player2.name)
                    TextField(
                        value = score2.toString(),
                        onValueChange = { score ->
                            results = results.toMutableList().apply {
                                val newScore = score.toIntOrNull() ?: 0
                                this[index] = this[index].first to (this[index].second.first to newScore)
                            }
                        },
                        modifier = Modifier.width(60.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Przycisk zakończenia rundy
        Button(
            onClick = { onRoundComplete(results) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Zakończ rundę")
        }

        // Przycisk powrotu do konfiguracji
        Button(
            onClick = onBackToSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Powrót do konfiguracji")
        }
    }
}
