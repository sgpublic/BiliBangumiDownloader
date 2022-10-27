package io.github.sgpublic.bilidownload.core.forest.data;

import androidx.annotation.Nullable;

import com.badlogic.gdx.utils.Null;

import java.util.ArrayList;
import java.util.List;

import io.github.sgpublic.bilidownload.base.forest.ResultResp;
import io.github.sgpublic.bilidownload.core.forest.data.common.BadgeInfo;
import io.github.sgpublic.bilidownload.core.forest.data.common.Nextable;
import io.github.sgpublic.bilidownload.core.forest.data.common.SeasonEpisodeBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Madray Haven
 * @date 2022/10/21 13:51
 */
public class FollowsResp extends ResultResp<FollowsResp.FollowsData> {
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class FollowsData extends Nextable {
        private List<FollowItem> followList;

        @Data
        @EqualsAndHashCode(callSuper = true)
        public static class FollowItem extends SeasonEpisodeBean {
            @Nullable
            private BadgeInfo badgeInfo;
            private String cover;
            @Nullable
            private Process process;
            private String title;

            @Data
            public static class Process {
                private long lastEpId;
            }
        }
    }
}
