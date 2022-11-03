package io.github.sgpublic.bilidownload.core.forest.data;

import java.util.ArrayList;
import java.util.List;

import io.github.sgpublic.bilidownload.base.forest.DataResp;
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

        @Data
        public static class TokenInfo {
            private String mid;
            private String accessToken;
            private String refreshToken;
            private int expiresIn;
        }

        @Data
        public static class CookieInfo {
            private List<Cookie> cookies = new ArrayList<>();
        }

        @Data
        public static class Cookie {
            private String name;
            private String value;
        }
    }
}
