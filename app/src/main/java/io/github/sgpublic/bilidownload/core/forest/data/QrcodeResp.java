package io.github.sgpublic.bilidownload.core.forest.data;

import io.github.sgpublic.bilidownload.base.forest.DataResp;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class QrcodeResp extends DataResp<QrcodeResp.QrcodeData> {
    @Data
    public static class QrcodeData {
        private String url;
        private String authCode;
    }
}
