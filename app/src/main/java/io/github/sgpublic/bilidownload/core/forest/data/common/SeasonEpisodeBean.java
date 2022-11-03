package io.github.sgpublic.bilidownload.core.forest.data.common;

import androidx.annotation.Nullable;

import java.util.Objects;

import lombok.Data;
import lombok.val;

/**
 * @author Madray Haven
 * @date 2022/10/24 11:32
 */
@Data
public class SeasonEpisodeBean {
    @Nullable
    private Long seasonId;
    @Nullable
    private Long episodeId;
    @Nullable
    private Long cid;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Long epid = getEpisodeId();
        return epid != null && epid.equals(((SeasonEpisodeBean) o).getEpisodeId());
    }

    public boolean equals(@Nullable Long epid) {
        return epid != null && epid.equals(getEpisodeId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getEpisodeId());
    }
}
