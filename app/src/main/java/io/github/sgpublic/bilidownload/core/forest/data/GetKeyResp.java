package io.github.sgpublic.bilidownload.core.forest.data;

import io.github.sgpublic.bilidownload.base.forest.DataResp;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class GetKeyResp extends DataResp<GetKeyResp.GetKeyData> {
    @Data
    public static class GetKeyData {
        private String hash;
        private String key;
    }
}
