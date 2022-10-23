package io.github.sgpublic.bilidownload.core.forest.data;

import io.github.sgpublic.bilidownload.base.forest.ResultResp;
import io.github.sgpublic.bilidownload.core.forest.annotations.ModuleStyle;
import io.github.sgpublic.bilidownload.core.forest.data.common.BadgeInfo;
import io.github.sgpublic.bilidownload.core.forest.data.common.Modules;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Madray Haven
 * @date 2022/10/21 10:54
 */
public class BannerResp extends ResultResp<BannerResp.BannerData> {
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class BannerData extends Modules {
        @ModuleStyle("banner_v3")
        @Data
        @EqualsAndHashCode(callSuper = true)
        public static class BannerItem extends ModuleItem<BannerItem.Item> {
            @Data
            public static class Item {
                private int aid;
                private BadgeInfo badgeInfo;
                private String cover;
                private String desc;
                private int seasonId;
                private String title;
            }
        }
    }
}
