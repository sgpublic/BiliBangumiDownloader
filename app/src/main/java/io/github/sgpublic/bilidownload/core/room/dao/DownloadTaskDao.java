package io.github.sgpublic.bilidownload.core.room.dao;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.github.sgpublic.bilidownload.core.room.entity.DownloadTaskEntity;

/**
 * @author Madray Haven
 * @date 2022/11/2 16:49
 */
@Dao
public interface DownloadTaskDao {
    /**
     * 仅用于添加任务
     * @param list 待添加的任务列表
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void add(List<DownloadTaskEntity> list);

    /**
     * 仅用于修改任务状态
     * @param task 修改后的任务 Entity
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void set(DownloadTaskEntity task);

    @Query("select * from download_task where sid=:sid")
    LiveData<List<DownloadTaskEntity>> observeBySid(long sid);

    @NonNull
    @Query("select * from download_task where task_id=:taskId")
    DownloadTaskEntity getByTaskId(long taskId);

    @Query("select * from download_task where status == 'Processing'")
    LiveData<List<DownloadTaskEntity>> observeProcessing();

    @Nullable
    @Query("select * from download_task where status == 'Waiting' or status == 'Retry' limit 1")
    DownloadTaskEntity getOneWaiting();

    @Query("update download_task set status='Waiting' where status == 'Processing'")
    void resetProcessing();
}
