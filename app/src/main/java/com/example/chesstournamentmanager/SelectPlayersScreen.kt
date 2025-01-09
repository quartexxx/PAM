package com.example.chesstournamentmanager

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chesstournamentmanager.data.Player

@Composable
fun SelectPlayersScreen(
    players: List<Player>,
    onPlayerSelected: (Player, Boolean) -> Unit,
    onProceed: () -> Unit,
    onBackToAddPlayers: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp), // Większy margines na całym ekranie
        verticalArrangement = Arrangement.spacedBy(16.dp), // Równe odstępy między elementami
        horizontalAlignment = Alignment.CenterHorizontally // Wyśrodkowanie elementów
    ) {
        Text(
            text = "Wybierz zawodników do turnieju",
            style = MaterialTheme.typography.headlineSmall // Większy, estetyczny nagłówek
        )

        // Lista zawodników z checkboxami
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp) // Równe odstępy między zawodnikami
        ) {
            players.forEach { player ->
                val isSelected = remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp), // Małe marginesy między zawodnikami
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = player.name)
                    Checkbox(
                        checked = isSelected.value,
                        onCheckedChange = { checked ->
                            isSelected.value = checked
                            onPlayerSelected(player, checked)
                        }
                    )
                }
            }
        }

        Button(
            onClick = onProceed,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Przejdź do ustawień turnieju")
        }

        Button(
            onClick = onBackToAddPlayers,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Powrót do dodawania zawodników")
        }
    }
}
