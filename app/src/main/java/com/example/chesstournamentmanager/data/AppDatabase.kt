package com.example.chesstournamentmanager.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Player::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
}
