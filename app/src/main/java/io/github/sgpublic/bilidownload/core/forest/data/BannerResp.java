package io.github.sgpublic.bilidownload.core.forest.data;

import com.google.gson.JsonObject;

import io.github.sgpublic.bilidownload.base.forest.ResultResp;
import io.github.sgpublic.bilidownload.core.forest.data.common.BadgeInfo;
import lombok.Data;

/**
 * @author Madray Haven
 * @date 2022/10/21 10:54
 */
public class BannerResp extends ResultResp<JsonObject> {
    @Data
    public static class BannerItem {
        private int aid;
        private BadgeInfo badgeInfo;
    }
}
