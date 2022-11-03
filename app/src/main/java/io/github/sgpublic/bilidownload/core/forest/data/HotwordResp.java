package io.github.sgpublic.bilidownload.core.forest.data;

import java.util.LinkedList;

import io.github.sgpublic.bilidownload.base.forest.DataResp;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class HotwordResp extends DataResp<LinkedList<HotwordResp.Hotword>> {
    @Data
    public static class Hotword {
        private int position;
        private String keyword;
        private String showName;
    }
}
