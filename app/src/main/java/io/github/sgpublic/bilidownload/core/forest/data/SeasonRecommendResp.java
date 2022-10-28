package io.github.sgpublic.bilidownload.core.forest.data;

import androidx.annotation.Nullable;

import com.badlogic.gdx.utils.Null;

import java.util.List;

import io.github.sgpublic.bilidownload.base.forest.ResultResp;
import io.github.sgpublic.bilidownload.core.forest.data.common.BadgeInfo;
import io.github.sgpublic.bilidownload.core.forest.data.common.Rating;
import io.github.sgpublic.bilidownload.core.forest.data.common.SeasonEpisodeBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Madray Haven
 * @date 2022/10/27 13:40
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SeasonRecommendResp extends ResultResp<SeasonRecommendResp.SeasonRecommend> {
    @Data
    public static class SeasonRecommend {
        private List<Card> cards;
        private String cardTitle;

        @Data
        public static class Card {
            @Nullable
            private Season season;
            @Nullable
            private Resource resource;
            /**
             * <p>推荐类型
             * <li>1: 番剧<li/>
             * <li>2: 漫画<li/>
             */
            private int type;

            @Data
            public static class Resource {
                private String cover;
                private String desc;
                private String label;
                private String reValue;
                private String title;
            }

            @Data
            @EqualsAndHashCode(callSuper = true)
            public static class Season extends SeasonEpisodeBean {
                @Nullable
                private BadgeInfo badgeInfo;
                private String cover;
                private NewEp newEp;
                private Rating rating;
                private String title;

                @Data
                public static class NewEp {
                    private String indexShow;
                }
            }
        }
    }
}
