package io.github.sgpublic.bilidownload.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.sgpublic.bilidownload.room.entity.TaskEntity

@Dao
interface TasksDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(task: List<TaskEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(task: TaskEntity)

    @Query("delete from tasks where cid=:cid")
    fun deleteByCid(cid: Long)

    @Query("delete from tasks where sid=:sid")
    fun deleteBySid(sid: Long)

    @Query("select * from tasks")
    fun getAll(): List<TaskEntity>

    @Query("select * from tasks where cid=:cid")
    fun getByCid(cid: Long): TaskEntity?

    @Query("select * from tasks where sid=:sid")
    fun getBySid(sid: Long): List<TaskEntity>

    @Query("select sid from tasks")
    fun getSids(): List<Long>

    @Query("select * from tasks where status=:status")
    fun getByTaskStatus(status: Int): List<TaskEntity>

    @Query("select * from tasks where taskId=:taskId")
    fun getByTaskId(taskId: Long): TaskEntity?

    @Query("update tasks set taskId=:taskId where cid=:cid")
    fun setTaskIdByCid(taskId: Long, cid: Long)

    @Query("select * from tasks")
    fun listenAll(): LiveData<List<TaskEntity>>

    @Query("select sid from tasks")
    fun listenAllSid(): LiveData<List<Long>>

    @Query("select * from tasks where status=:status")
    fun listenByTaskStatus(status: Int): LiveData<List<TaskEntity>>

    @Query("select * from tasks where cid=:cid")
    fun listenByCid(cid: Long): LiveData<TaskEntity>

    @Query("select status from tasks where cid=:cid")
    fun listenStatusByCid(cid: Long): LiveData<Int>

    @Query("select * from tasks where sid=:sid")
    fun listenBySid(sid: Long): LiveData<List<TaskEntity>>

    @Query("select * from tasks where cid=:cid")
    fun listenSingleTask(cid: Long): LiveData<List<TaskEntity>>

    @Query("update tasks set status=:status, message='' where cid=:cid")
    fun updateStatusByCid(cid: Long, status: Int)

    @Query("update tasks set status=:code, message=:message where cid=:cid")
    fun setErrorMessageByCid(cid: Long, code: Int, message: String)

    @Query("update tasks set status=${TaskEntity.STATUS_WAITING} where status=${TaskEntity.STATUS_PREPARING}")
    fun resetPreparingTasks()
    @Query("update tasks set status=${TaskEntity.STATUS_PAUSED} where status=${TaskEntity.STATUS_PROCESSING}")
    fun resetProcessingTasks()
}