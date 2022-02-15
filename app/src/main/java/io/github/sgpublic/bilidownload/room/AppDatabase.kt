package io.github.sgpublic.bilidownload.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.sgpublic.bilidownload.room.converter.DashIndexJsonConverter
import io.github.sgpublic.bilidownload.room.converter.EntryJsonConverter
import io.github.sgpublic.bilidownload.room.dao.TasksDao
import io.github.sgpublic.bilidownload.room.dao.WatchHistoryDao
import io.github.sgpublic.bilidownload.room.entity.TaskEntity
import io.github.sgpublic.bilidownload.room.entity.WatchHistoryEntity

@Database(entities = [TaskEntity::class, WatchHistoryEntity::class], version = 1)
@TypeConverters(DashIndexJsonConverter::class, EntryJsonConverter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun TasksDao(): TasksDao
    abstract fun WatchHistoryDao(): WatchHistoryDao
}