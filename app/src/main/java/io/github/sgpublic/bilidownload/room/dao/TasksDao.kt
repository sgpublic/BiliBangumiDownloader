package io.github.sgpublic.bilidownload.room.dao

import androidx.room.*
import io.github.sgpublic.bilidownload.room.entity.TaskEntity

@Dao
interface TasksDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(task: List<TaskEntity>)

    @Delete
    fun delete(task: TaskEntity)

    @Query("select * from tasks where cid=:cid")
    fun getByCid(cid: Long): TaskEntity?

    @Query("select * from tasks where status=:status")
    fun getByTaskStatus(status: Int): List<TaskEntity>

    @Query("update tasks set status=:status where cid=:cid")
    fun updateStatusByCid(cid: Long, status: Int)
}