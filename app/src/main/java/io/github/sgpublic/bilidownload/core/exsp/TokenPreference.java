package io.github.sgpublic.bilidownload.core.exsp;

import io.github.sgpublic.exsp.annotations.ExSharedPreference;
import io.github.sgpublic.exsp.annotations.ExValue;
import lombok.Data;

@Data
@ExSharedPreference(name = "token")
public class TokenPreference {
    @ExValue(defVal = "false")
    private boolean login;

    @ExValue(defVal = "")
    private String buvid;

    @ExValue(defVal = "")
    private String loginSessionId;

    @ExValue(defVal = "")
    private String accessToken;

    @ExValue(defVal = "")
    private String refreshToken;

    @ExValue(defVal = "-1")
    private long tokenExpired;



    @ExValue(defVal = "")
    private String cookieBiliJct;

    @ExValue(defVal = "")
    private String cookieDedeUserID;

    @ExValue(defVal = "")
    private String cookieDedeUserID_ckMd5;

    @ExValue(defVal = "")
    private String cookieSid;

    @ExValue(defVal = "")
    private String cookieSESSDATA;
}
