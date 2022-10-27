package io.github.sgpublic.bilidownload.core.forest.data.common;

import java.util.Objects;

import lombok.Data;

/**
 * @author Madray Haven
 * @date 2022/10/24 11:32
 */
@Data
public class SeasonEpisodeBean {
    private long seasonId;
    private long episodeId;
    private long cid;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return getEpisodeId() == ((SeasonEpisodeBean) o).getEpisodeId();
    }

    public boolean equals(long epid) {
        return getEpisodeId() == epid;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getEpisodeId());
    }
}
