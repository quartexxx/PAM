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
    onRoundComplete: (List<Pair<Pair<Player, Player>, Pair<Float, Float>>>) -> Unit,
    onBackToSettings: () -> Unit,
    system: String // Dodano parametr systemu
) {
    var results by remember {
        mutableStateOf(pairs.map { pair ->
            if (pair.first.isBye || pair.second.isBye) {
                if (pair.first.isBye) pair to (0f to 1f)
                else pair to (1f to 0f) // Automatyczne przyznanie punktów dla "Wolnego Losu"
            } else {
                pair to (0f to 0f) // Domyślny wynik: remis
            }
        })
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

        results.forEachIndexed { index, pairResult ->
            val (player1, player2) = pairResult.first
            val (score1, score2) = pairResult.second

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = player1.name)
                    Text(text = "Punkty: $score1")
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (!player1.isBye && !player2.isBye) {
                        Button(onClick = {
                            results = results.toMutableList().apply {
                                this[index] = this[index].first to (1f to 0f) // 1:0
                            }
                        }) {
                            Text("1:0")
                        }
                        Button(onClick = {
                            results = results.toMutableList().apply {
                                this[index] = this[index].first to (0f to 1f) // 0:1
                            }
                        }) {
                            Text("0:1")
                        }
                        Button(onClick = {
                            results = results.toMutableList().apply {
                                this[index] = this[index].first to (0.5f to 0.5f) // 0.5:0.5
                            }
                        }) {
                            Text("0.5:0.5")
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = player2.name)
                    Text(text = "Punkty: $score2")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onRoundComplete(results) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Zakończ rundę")
        }

        Button(
            onClick = onBackToSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Powrót do konfiguracji")
        }
    }
}


