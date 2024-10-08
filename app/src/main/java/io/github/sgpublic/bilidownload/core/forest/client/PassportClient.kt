package io.github.sgpublic.bilidownload.core.forest.client

import com.dtflys.forest.annotation.*
import com.dtflys.forest.http.ForestRequest
import io.github.sgpublic.bilidownload.core.forest.annotations.BiliSign
import io.github.sgpublic.bilidownload.core.forest.data.CaptchaResp
import io.github.sgpublic.bilidownload.core.forest.data.CountryResp
import io.github.sgpublic.bilidownload.core.forest.data.GetKeyResp
import io.github.sgpublic.bilidownload.core.forest.data.LoginResp
import io.github.sgpublic.bilidownload.core.forest.data.QrcodePollResp
import io.github.sgpublic.bilidownload.core.forest.data.QrcodeResp
import io.github.sgpublic.bilidownload.core.forest.data.SmsSendResp

@Address(
    scheme = "https",
    host = "passport.bilibili.com"
)
interface PassportClient {
    @Get("/x/passport-login/captcha")
    fun captcha(): ForestRequest<CaptchaResp>

    @BiliSign(appKey = appKey, appSecret = appSecret)
    @Get("/x/passport-login/web/key")
    fun pubKey(): ForestRequest<GetKeyResp>

    @Deprecated("Use sms sending instead.")
    @BiliSign(appKey = appKey, appSecret = appSecret)
    @Post("/x/passport-login/oauth2/login")
    fun login(
        @Body("username") username: String,
        @Body("password") pwdEncrypted: String,
        @Body("from_pv") fromPv: String = "main.homepage.avatar-nologin.all.click",
        @Header("app-key") appKey: String = "android64",
    ) : ForestRequest<LoginResp>

    @Deprecated("Use sms sending instead.")
    @BiliSign(appKey = appKey, appSecret = appSecret)
    @Post("/x/passport-login/oauth2/login")
    fun geetestLogin(
        @Body("recaptcha_token") token: String,
        @Body("gee_challenge") challenge: String,
        @Body("gee_validate") validate: String,
        @Body("gee_seccode") seccode: String,
        @Body("username") username: String,
        @Body("password") pwdEncrypted: String,
        @Body("from_pv") fromPv: String = "main.my-information.my-login.0.click",
        @Body("spm_id") spmId: String = "main.my-information.my-login.0",
        @Body("device_name") deviceName: String = "Android",
        @Header("app-key") appKey: String = "android64",
    ) : ForestRequest<LoginResp>

    @Get("/web/generic/country/list")
    fun countryList(): ForestRequest<CountryResp>

    @BiliSign(appKey = appKey, appSecret = appSecret)
    @Post("/x/passport-login/sms/send")
    fun smsSend(
        @Body("cid") cid: Int,
        @Body("tel") tel: Long,
        @Body("login_session_id") loginSessionId: String,
        @Body("recaptcha_token") token: String,
        @Body("gee_challenge") challenge: String,
        @Body("gee_validate") validate: String,
        @Body("gee_seccode") seccode: String,
        @Body("buvid") buvid: String,
        @Body("channel") channel: String = "bili",
        @Body("local_id") localId: String = buvid,
        @URLEncode @Body("statistics") statistics: String = PassportClient.statistics,
    ): ForestRequest<SmsSendResp>

    @BiliSign(appKey = appKey, appSecret = appSecret)
    @Post("/x/passport-tv-login/qrcode/auth_code")
    fun qrcodeTv(
        @Body("local_id") localId: Int = tvLocalId,
    ): ForestRequest<QrcodeResp>

    @BiliSign(appKey = appKey, appSecret = appSecret)
    @Post("/x/passport-tv-login/qrcode/poll")
    fun qrcodeTvPoll(
        @Body("auth_code") authCode: String,
        @Body("local_id") localId: Int = tvLocalId,
    ): ForestRequest<LoginResp>

    @BiliSign(appKey = appKey, appSecret = appSecret)
    @Get("/api/v2/oauth2/access_token")
    fun accessToken(
        @Query("code") code: String,
        @Query("grant_type") grantType: String = "volatile_code"
    ): ForestRequest<LoginResp>

    @BiliSign(appKey = appKey, appSecret = appSecret)
    @Post("/api/oauth2/refreshToken")
    fun refreshToken(
        @Body("access_token") accessToken: String,
        @Body("refresh_token") refreshToken: String,
    ): ForestRequest<String>

    companion object {
        const val appKey = "783bbb7264451d82"
        const val appSecret = "2653583c8873dea268ab9386918b1d65"
        const val statistics = "{\"appId\":1,\"platform\":3,\"version\":\"7.27.0\",\"abtest\":\"\"}"
        const val tvLocalId = 0
    }
}