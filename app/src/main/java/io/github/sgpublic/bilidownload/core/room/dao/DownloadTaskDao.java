package io.github.sgpublic.bilidownload.core.room.dao;

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
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void save(List<DownloadTaskEntity> list);

    @Query("select * from download_task where sid=:sid")
    LiveData<List<DownloadTaskEntity>> observeBySid(long sid);

    @Query("select * from download_task where status != 'Finished'")
    LiveData<List<DownloadTaskEntity>> observeAll();
}
