package io.github.sgpublic.bilidownload.core.forest.data;

import io.github.sgpublic.bilidownload.base.forest.DataResp;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SmsSendResp extends DataResp<SmsSendResp.SmsSendData> {
    @Data
    public static class SmsSendData {
        private String captchaKey;
    }
}
