package io.github.sgpublic.bilidownload.core.forest.data.common;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CookieInfo {
    private List<Cookie> cookies = new ArrayList<>();

    @Data
    public static class Cookie {
        private String name;
        private String value;
    }
}
