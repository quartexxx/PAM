package com.example.chesstournamentmanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chesstournamentmanager.data.Player

@Composable
fun RoundResultsScreen(
    roundResults: List<Pair<Pair<Player, Player>, Pair<Float, Float>>>,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Wyniki rund",
            style = MaterialTheme.typography.headlineSmall
        )

        roundResults.chunked(2).forEachIndexed { index, results ->
            Text(text = "Runda ${index + 1}:", style = MaterialTheme.typography.titleMedium)
            results.forEach { (pair, scores) ->
                Text(text = "${pair.first.name} (${scores.first}) vs ${pair.second.name} (${scores.second})")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Powrót")
        }
    }
}
