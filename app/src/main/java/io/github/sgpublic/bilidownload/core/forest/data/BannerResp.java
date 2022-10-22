package io.github.sgpublic.bilidownload.core.forest.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

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
public class BannerResp extends ResultResp<JsonArray> {
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
