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
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    content = { innerPadding ->
                        AddPlayerScreen(
                            modifier = Modifier.padding(innerPadding),
                            onAddPlayer = { name, rating ->
                                lifecycleScope.launch {
                                    db.playerDao().insert(Player(name = name, rating = rating))
                                    println("Dodano zawodnika: $name, Ranking: ${rating ?: "Brak"}")
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
    onAddPlayer: (String, Int?) -> Unit
) {
    var playerName = remember { mutableStateOf("") }
    var playerRating = remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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
    }
}
