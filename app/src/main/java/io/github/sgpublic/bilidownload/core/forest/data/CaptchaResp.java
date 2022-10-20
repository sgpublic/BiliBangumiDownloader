package io.github.sgpublic.bilidownload.core.forest.data;

import io.github.sgpublic.bilidownload.base.forest.DataResp;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CaptchaResp extends DataResp<CaptchaResp.CaptchaData> {
    @Data
    public static class CaptchaData {
        private String url;

        private String token;
        private CaptchaDataGeetest geetest = new CaptchaDataGeetest();

        @Data
        public static class CaptchaDataGeetest {
            private String gt;
            private String challenge;
        }
    }
}
