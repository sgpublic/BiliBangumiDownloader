package io.github.sgpublic.bilidownload.core.forest.data;

import java.util.List;

import io.github.sgpublic.bilidownload.base.forest.ResultResp;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SearchSuggestResp extends ResultResp<List<SearchSuggestResp.Tag>> {
    @Data
    public static class Tag {
        private String value;
        private String name;
    }
}
