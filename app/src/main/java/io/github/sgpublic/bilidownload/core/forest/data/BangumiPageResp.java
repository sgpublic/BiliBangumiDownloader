package io.github.sgpublic.bilidownload.core.forest.data;

import com.google.gson.JsonArray;

import java.util.List;

import io.github.sgpublic.bilidownload.base.forest.ResultResp;
import io.github.sgpublic.bilidownload.core.forest.annotations.ApiStyle;
import io.github.sgpublic.bilidownload.core.forest.data.common.BadgeInfo;
import io.github.sgpublic.bilidownload.core.forest.data.common.TextBadge;
import lombok.Data;

/**
 * @author Madray Haven
 * @date 2022/10/21 14:23
 */
public class BangumiPageResp extends ResultResp<BangumiPageResp.BangumiPageData> {
    @Data
    public static class BangumiPageData {
        /** 是否有后续 */
        private int hasNext;
        /** 下一页索引 */
        private int nextCursor;
        /** 模块 */
        private JsonArray modules;

        @Data
        @ApiStyle("fall_feed")
        public static class FallFeed {
            private List<Item> items;

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
        @ApiStyle("double_feed")
        public static class DoubleFeed {
            private List<Item> items;

            @Data
            public static class Item {
                private BadgeInfo badgeInfo;
                private TextBadge bottomLeftBadge;
                private String cover;
                private String desc;
            }
        }
    }
}
