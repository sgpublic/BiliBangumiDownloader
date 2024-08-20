package io.github.sgpublic.bilidownload.core.forest.data;

import io.github.sgpublic.bilidownload.base.forest.DataResp;
import io.github.sgpublic.bilidownload.core.forest.data.common.CookieInfo;
import io.github.sgpublic.bilidownload.core.forest.data.common.TokenInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class LoginResp extends DataResp<LoginResp.LoginData> {
    @Data
    public static class LoginData {
        private int status;
        private String message;
        private String url;
        private TokenInfo tokenInfo = new TokenInfo();
        private CookieInfo cookieInfo = new CookieInfo();
    }
}
