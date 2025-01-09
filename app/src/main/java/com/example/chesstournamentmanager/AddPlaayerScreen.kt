package com.example.chesstournamentmanager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.chesstournamentmanager.data.Player
import androidx.compose.ui.Alignment

@Composable
fun AddPlayerScreen(
    players: List<Player>,
    onAddPlayer: (String, Int?) -> Unit,
    onClearPlayers: () -> Unit,
    onNavigateToSelectPlayers: () -> Unit
) {
    var playerName = remember { mutableStateOf("") }
    var playerRating = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp), // Większy margines na całym ekranie
        verticalArrangement = Arrangement.spacedBy(16.dp), // Równe odstępy między elementami
        horizontalAlignment = Alignment.CenterHorizontally // Wyśrodkowanie elementów
    ) {
        Text(
            text = "Dodaj zawodnika",
            style = MaterialTheme.typography.headlineSmall // Większy, estetyczny nagłówek
        )

        TextField(
            value = playerName.value,
            onValueChange = { playerName.value = it },
            label = { Text("Imię i nazwisko zawodnika") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = playerRating.value,
            onValueChange = { playerRating.value = it },
            label = { Text("Ranking (opcjonalny)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Button(
            onClick = {
                val name = playerName.value.trim()
                val rating = playerRating.value.toIntOrNull()

                if (name.isNotEmpty()) {
                    onAddPlayer(name, rating)
                    playerName.value = ""
                    playerRating.value = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Dodaj zawodnika")
        }

        Button(
            onClick = onClearPlayers,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Wyczyść listę zawodników")
        }

        Button(
            onClick = onNavigateToSelectPlayers,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Przejdź do wyboru zawodników")
        }
    }
}
