package io.github.sgpublic.bilidownload.core.room

import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.sgpublic.bilidownload.core.room.dao.WatchHistoryDao
import io.github.sgpublic.bilidownload.core.room.entity.WatchHistoryEntity

@Database(entities = [WatchHistoryEntity::class], version = 4)
abstract class AppDatabase: RoomDatabase() {
    abstract fun WatchHistoryDao(): WatchHistoryDao
}