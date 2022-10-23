package io.github.sgpublic.bilidownload.core.forest.data;

import io.github.sgpublic.bilidownload.base.forest.ResultResp;
import io.github.sgpublic.bilidownload.core.forest.annotations.ModuleStyle;
import io.github.sgpublic.bilidownload.core.forest.data.common.BadgeInfo;
import io.github.sgpublic.bilidownload.core.forest.data.common.Modules;
import io.github.sgpublic.bilidownload.core.forest.data.common.TextBadge;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Madray Haven
 * @date 2022/10/21 14:23
 */
public class BangumiPageResp extends ResultResp<BangumiPageResp.BangumiPageData> {
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class BangumiPageData extends Modules {
        /** 是否有后续 */
        private int hasNext;
        /** 下一页索引 */
        private int nextCursor;

        @Data
        @EqualsAndHashCode(callSuper = true)
        @ModuleStyle("fall_feed")
        public static class FallFeed extends ModuleItem<FallFeed.Item> {
            @Data
            public static class Item {
                private BadgeInfo badgeInfo;
                private String cover;
                private String desc;
                private int episodeId;
                private int seasonId;
                private String title;
                private TextBadge textRightBadge;
            }
        }

        @Data
        @EqualsAndHashCode(callSuper = true)
        @ModuleStyle("double_feed")
        public static class DoubleFeed extends ModuleItem<FallFeed.Item> {
            @Data
            public static class Item {
                private BadgeInfo badgeInfo;
                private TextBadge bottomLeftBadge;
                private String cover;
                private String desc;
                private String type;
            }
        }

        @Data
        @EqualsAndHashCode(callSuper = true)
        @ModuleStyle("follow")
        public static class Follow extends Modules {
            @Data
            public static class Item {
                private String desc;
                private int descType;
                private NewEp newEp;
                private int seasonId;

                @Data
                public static class NewEp {
                    private String cover;
                    private String indexShow;
                }
            }
        }
    }
}
