package io.github.sgpublic.bilidownload

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sgpublic.bilidownload.room.AppDatabase
import io.github.sgpublic.bilidownload.room.entity.TaskEntity
import io.github.sgpublic.bilidownload.util.MyLog
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomTest: ApplicationText() {
    private lateinit var database: AppDatabase

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            applicationContext, AppDatabase::class.java
        ).build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun readDatabase() {
        val task = database.TasksDao()
        MyLog.d(task.getByTaskStatus(TaskEntity.STATUS_WAITING))
    }
}