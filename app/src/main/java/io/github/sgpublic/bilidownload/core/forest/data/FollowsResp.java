package io.github.sgpublic.bilidownload.core.forest.data;

import com.google.gson.JsonArray;

import io.github.sgpublic.bilidownload.base.forest.ResultResp;
import lombok.Data;

/**
 * @author Madray Haven
 * @date 2022/10/21 13:51
 */
public class FollowsResp extends ResultResp<FollowsResp.FollowsData> {
    @Data
    public static class FollowsData {
        private JsonArray modules;


    }
}
