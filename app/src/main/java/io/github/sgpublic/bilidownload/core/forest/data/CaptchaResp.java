package io.github.sgpublic.bilidownload.core.forest.data;

import io.github.sgpublic.bilidownload.base.forest.DataResp;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CaptchaResp extends DataResp<CaptchaResp.CaptchaData> {
    @Data
    public static class CaptchaData {
        /** 备用方案，直接打开一个 dialog 打开阿b给的极验链接 */
        private String token;
        private CaptchaDataGeetest geetest = new CaptchaDataGeetest();

        @Data
        public static class CaptchaDataGeetest {
            private String gt;
            private String challenge;
        }
    }
}
