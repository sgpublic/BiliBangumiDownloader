package io.github.sgpublic.bilidownload.core.room;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import io.github.sgpublic.bilidownload.core.room.dao.DownloadTaskDao;
import io.github.sgpublic.bilidownload.core.room.entity.DownloadTaskEntity;
import kotlin.Lazy;
import kotlin.LazyKt;

/**
 * @author Madray Haven
 * @date 2022/11/2 16:55
 */
@Database(
        entities = { DownloadTaskEntity.class },
        version = 6, exportSchema = false
)
@TypeConverters({
        DownloadTaskEntity.Status.Converter.class
})
public abstract class AppDatabase extends RoomDatabase {
    public abstract DownloadTaskDao DownloadTaskDao();
}