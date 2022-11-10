package io.github.sgpublic.bilidownload.core.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import io.github.sgpublic.bilidownload.core.forest.ApiModule;
import lombok.Data;

/**
 * @author Madray Haven
 * @date 2022/11/2 15:55
 */
@Data
@Entity(tableName = "download_task")
public class DownloadTaskEntity {
    @PrimaryKey
    @ColumnInfo(name = "epid")
    private long epid;

    @ColumnInfo(name = "cid")
    private long cid;

    @ColumnInfo(name = "ep_title")
    private String episodeTitle;

    @ColumnInfo(name = "ep_cover")
    private String episodeCover;

    @ColumnInfo(name = "sid")
    private long sid;

    @ColumnInfo(name = "ss_title")
    private String seasonTitle;

    @ColumnInfo(name = "ss_cover")
    private String seasonCover;

    @ColumnInfo(name = "qn")
    private int qn = 80;

    @ColumnInfo(name = "task_id")
    private long taskId = -1L;

    @ColumnInfo(name = "status")
    private Status status = Status.Waiting;

    @ColumnInfo(name = "retry")
    private boolean retry = false;

    @ColumnInfo(name = "status_message")
    private String statusMessage = "";

    @ColumnInfo(name = "add_time")
    private long addTime = ApiModule.INSTANCE.getTS_FULL();

    public enum Status {
        Waiting, Prepare, Processing, Paused, Error, Finished, Canceled;

        public static class Converter {
            @TypeConverter
            public Status toStatus(String value) {
                return Status.valueOf(value);
            }

            @TypeConverter
            public String fromStatus(Status value) {
                return value.name();
            }
        }
    }
}
