package io.github.sgpublic.bilidownload.core.forest.client

import com.dtflys.forest.annotation.*
import com.dtflys.forest.http.ForestRequest
import io.github.sgpublic.bilidownload.core.forest.annotations.BiliSign
import io.github.sgpublic.bilidownload.core.forest.data.CaptchaResp
import io.github.sgpublic.bilidownload.core.forest.data.GetKeyResp
import io.github.sgpublic.bilidownload.core.forest.data.LoginResp

@Address(
    scheme = "https",
    host = "passport.bilibili.com"
)
interface PassportClient {
    @Get("/x/passport-login/captcha")
    fun captcha(): ForestRequest<CaptchaResp>

    @BiliSign(
        appKey = "783bbb7264451d82",
        appSecret = "2653583c8873dea268ab9386918b1d65",
    )
    @Get("/x/passport-login/web/key")
    fun pubKey(): ForestRequest<GetKeyResp>

    @BiliSign(
        appKey = "783bbb7264451d82",
        appSecret = "2653583c8873dea268ab9386918b1d65",
    )
    @Post("/x/passport-login/oauth2/login")
    fun login(
        @Body("username") username: String,
        @Body("password") pwdEncrypted: String,
    ) : ForestRequest<LoginResp>

    @BiliSign(
        appKey = "783bbb7264451d82",
        appSecret = "2653583c8873dea268ab9386918b1d65",
    )
    @Post("/x/passport-login/oauth2/login")
    fun geetestLogin(
        @Body("recaptcha_token") token: String,
        @Body("gee_challenge") challenge: String,
        @Body("gee_validate") validate: String,
        @Body("gee_seccode") seccode: String,
        @Body("username") username: String,
        @Body("password") pwdEncrypted: String,
    ) : ForestRequest<LoginResp>

    @BiliSign(
        appKey = "783bbb7264451d82",
        appSecret = "2653583c8873dea268ab9386918b1d65",
    )
    @Get("/api/v2/oauth2/access_token")
    fun accessToken(
        @Query("code") code: String,
        @Query("grant_type") grantType: String = "volatile_code"
    ): ForestRequest<LoginResp>

    @BiliSign(
        appKey = "783bbb7264451d82",
        appSecret = "2653583c8873dea268ab9386918b1d65",
    )
    @Post("/api/oauth2/refreshToken")
    fun refreshToken(
        @Body("access_token") accessToken: String,
        @Body("refresh_token") refreshToken: String,
    ): ForestRequest<String>
}