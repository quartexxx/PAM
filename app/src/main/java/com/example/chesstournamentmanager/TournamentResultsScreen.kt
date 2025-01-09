package com.example.chesstournamentmanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chesstournamentmanager.data.Player

@Composable
fun TournamentResultsScreen(
    players: List<Player>,
    onBackToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Wyniki Końcowe Turnieju",
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
        )

        // Wyświetlanie listy zawodników i ich wyników
        players.sortedByDescending { it.rating ?: 0 }.forEach { player ->
            Text(
                text = "${player.name}: ${player.rating ?: "Brak rankingu"} punktów",
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Przycisk powrotu do głównego ekranu
        Button(
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Powrót do ekranu głównego")
        }
    }
}
