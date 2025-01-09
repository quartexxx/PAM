package com.example.chesstournamentmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.chesstournamentmanager.data.AppDatabase
import com.example.chesstournamentmanager.data.Player
import com.example.chesstournamentmanager.ui.theme.ChessTournamentManagerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "chess-database"
        ).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChessTournamentManagerTheme {
                val players = remember { mutableStateListOf<Player>() } // Lista zawodników

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    content = { innerPadding ->
                        AddPlayerScreen(
                            modifier = Modifier.padding(innerPadding),
                            onAddPlayer = { name, rating ->
                                lifecycleScope.launch {
                                    val player = Player(name = name, rating = rating)
                                    db.playerDao().insert(player) // Dodanie zawodnika do bazy
                                    players.clear()
                                    players.addAll(db.playerDao().getAllPlayers()) // Pobranie wszystkich zawodników
                                }
                            },
                            players = players, // Przekazanie listy zawodników
                            onClearPlayers = {
                                lifecycleScope.launch {
                                    db.playerDao().deleteAllPlayers() // Usunięcie wszystkich zawodników z bazy
                                    players.clear() // Wyczyszczenie listy w pamięci
                                }
                            }
                        )
                    }
                )

            }
        }
    }
}

@Composable
fun AddPlayerScreen(
    modifier: Modifier = Modifier,
    onAddPlayer: (String, Int?) -> Unit,
    players: List<Player>,
    onClearPlayers: () -> Unit // Dodaj obsługę czyszczenia
) {
    var playerName = remember { mutableStateOf("") }
    var playerRating = remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Pole tekstowe: Imię i nazwisko zawodnika
        TextField(
            value = playerName.value,
            onValueChange = { playerName.value = it },
            label = { Text("Imię i nazwisko zawodnika") },
            modifier = Modifier.fillMaxWidth()
        )

        // Pole tekstowe: Ranking
        TextField(
            value = playerRating.value,
            onValueChange = { playerRating.value = it },
            label = { Text("Ranking (opcjonalny)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Przycisk "Dodaj zawodnika"
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

        // Przycisk "Wyczyść listę zawodników"
        Button(
            onClick = { onClearPlayers() }, // Wywołanie funkcji czyszczenia
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Wyczyść listę zawodników")
        }

        // Wyświetlanie listy zawodników
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Lista zawodników:")
            players.forEach { player ->
                Text(text = "${player.name} (Ranking: ${player.rating ?: "Brak"})")
            }
        }
    }
}

