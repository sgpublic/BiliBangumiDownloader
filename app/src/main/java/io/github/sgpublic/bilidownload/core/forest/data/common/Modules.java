package io.github.sgpublic.bilidownload.core.forest.data.common;

import com.google.gson.JsonArray;

import java.util.List;

import lombok.Data;

/**
 * @author Madray Haven
 * @date 2022/10/22 17:03
 */
@Data
public class Modules {
    private JsonArray modules;

    @Data
    public static class ModuleItem<T> {
        private List<T> items;
    }
}

