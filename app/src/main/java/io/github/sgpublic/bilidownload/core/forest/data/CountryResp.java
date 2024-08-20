package io.github.sgpublic.bilidownload.core.forest.data;

import java.util.LinkedList;

import io.github.sgpublic.bilidownload.base.forest.DataResp;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CountryResp extends DataResp<CountryResp.CountryData> {
    @Data
    public static class CountryData {
        private LinkedList<CountryItem> common;
    }
    @Data
    public static class CountryItem {
        private int id;
        private String cname;
        private String countryId;
    }
}
