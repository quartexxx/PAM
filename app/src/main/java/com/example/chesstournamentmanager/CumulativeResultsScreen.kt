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
fun CumulativeResultsScreen(
    cumulativeResults: Map<Player, Float>,
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
            text = "Bieżące wyniki zbiorcze",
            style = MaterialTheme.typography.headlineSmall
        )

        cumulativeResults.entries.sortedByDescending { it.value }.forEach { (player, score) ->
            Text(text = "${player.name}: ${score} pkt")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Powrót")
        }
    }
}
