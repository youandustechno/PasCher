package com.monasoftware.pascher.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.monasoftware.pascher.data.local.dao.MovieDao
import com.monasoftware.pascher.data.local.entity.MovieEntity

@Database(entities = [MovieEntity::class], version = 1, exportSchema = false)
abstract class PasCherDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao

    companion object {
        const val DATABASE_NAME = "pascher_db"
    }
}
