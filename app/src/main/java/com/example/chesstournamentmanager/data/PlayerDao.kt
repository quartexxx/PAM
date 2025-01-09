package com.example.chesstournamentmanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PlayerDao {
    @Insert
    suspend fun insert(player: Player)

    @Query("SELECT * FROM players")
    suspend fun getAllPlayers(): List<Player>

    @Query("DELETE FROM players")
    suspend fun deleteAllPlayers()
}
