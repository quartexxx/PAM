package com.example.chesstournamentmanager

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chesstournamentmanager.data.Player

@Composable
fun SelectPlayersScreen(
    players: List<Player>,
    onPlayerSelected: (Player, Boolean) -> Unit,
    onProceed: () -> Unit,
    onBackToAddPlayers: () -> Unit, // Dodany parametr do obsługi powrotu
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Wybierz zawodników do turnieju:")

        // Lista zawodników z checkboxami
        players.forEach { player ->
            val isSelected = remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.fillMaxWidth(),
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

        // Przycisk do przejścia dalej
        Button(
            onClick = onProceed,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Przejdź do ustawień turnieju")
        }

        // Przycisk do powrotu na ekran dodawania zawodników
        Button(
            onClick = onBackToAddPlayers, // Wywołanie powrotu
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Powrót do dodawania zawodników")
        }
    }
}
