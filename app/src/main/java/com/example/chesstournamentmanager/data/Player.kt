package com.example.chesstournamentmanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class Player(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val rating: Int? = null,
    val isBye: Boolean = false
)
