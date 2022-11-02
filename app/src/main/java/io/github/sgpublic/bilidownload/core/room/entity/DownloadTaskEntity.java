package io.github.sgpublic.bilidownload.core.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

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

    @ColumnInfo(name = "episode_cover")
    private String episodeCover;

    @ColumnInfo(name = "sid")
    private long sid;

    @ColumnInfo(name = "season_cover")
    private String seasonCover;

    @ColumnInfo(name = "task_ids")
    private List<Long> taskIds = new ArrayList<>();

    @ColumnInfo(name = "status")
    private Status status = Status.Waiting;

    @ColumnInfo(name = "add_time")
    private long addTime = ApiModule.INSTANCE.getTS_FULL();

    public enum Status {
        Waiting, Processing, Paused, Error, Finished;

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

    public static class TaskIdsConverter {
        @TypeConverter
        public String toTaskIds(List<Long> value) {
            StringJoiner joiner = new StringJoiner(",");
            for (Long aLong : value) {
                joiner.add(aLong.toString());
            }
            return joiner.toString();
        }

        @TypeConverter
        public List<Long> fromTaskIds(String value) {
            if (value.isBlank()) return new ArrayList<>();
            String[] ids = value.split(",");
            ArrayList<Long> result = new ArrayList<>(ids.length);
            for (String id : ids) {
                result.add(Long.parseLong(id));
            }
            return result;
        }
    }
}
