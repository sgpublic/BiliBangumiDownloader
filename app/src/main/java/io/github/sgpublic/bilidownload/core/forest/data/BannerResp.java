package io.github.sgpublic.bilidownload.core.forest.data;

import androidx.annotation.Nullable;

import io.github.sgpublic.bilidownload.base.forest.ResultResp;
import io.github.sgpublic.bilidownload.core.forest.annotations.ModuleStyle;
import io.github.sgpublic.bilidownload.core.forest.data.common.BadgeInfo;
import io.github.sgpublic.bilidownload.core.forest.data.common.Modules;
import io.github.sgpublic.bilidownload.core.forest.data.common.SeasonEpisodeBean;
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
            @EqualsAndHashCode(callSuper = true)
            public static class Item extends SeasonEpisodeBean {
                @Nullable
                private BadgeInfo badgeInfo;
                private String cover;
                private String desc;
                private String title;
            }
        }
    }
}
