package io.github.sgpublic.bilidownload.core.forest.data;

import io.github.sgpublic.bilidownload.base.forest.DataResp;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class QrcodePollResp extends DataResp<QrcodePollResp.QrcodePollData> {
    @Data
    public static class QrcodePollData {
        private int code;
        private String refreshToken;
    }
}
