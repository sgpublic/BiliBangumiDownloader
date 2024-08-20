package io.github.sgpublic.bilidownload.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dtflys.forest.http.ForestRequest
import com.google.gson.JsonObject
import io.github.sgpublic.bilidownload.base.app.BaseViewModel
import io.github.sgpublic.bilidownload.base.app.postValue
import io.github.sgpublic.bilidownload.core.forest.core.BiliApiException
import io.github.sgpublic.bilidownload.core.forest.data.CaptchaResp
import io.github.sgpublic.bilidownload.core.forest.data.CountryResp
import io.github.sgpublic.bilidownload.core.forest.data.GetKeyResp
import io.github.sgpublic.bilidownload.core.forest.data.LoginResp
import io.github.sgpublic.bilidownload.core.forest.data.SmsSendResp
import io.github.sgpublic.bilidownload.core.forest.data.UserInfoResp
import io.github.sgpublic.bilidownload.core.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*
import java.util.regex.Pattern
import javax.crypto.Cipher

class LoginSmsModel: BaseViewModel() {
    val CodeCd: MutableLiveData<Int> = MutableLiveData(-1)
    val CountryData: MutableLiveData<Map<Int, CountryResp.CountryItem>> by lazy {
        getCountryCode()
        MutableLiveData()
    }
    var CountrySelected: Int = -1

    val CaptchaData: MutableLiveData<CaptchaResp.CaptchaData> = MutableLiveData()
    val LoginData: MutableLiveData<LoginResp.LoginData> = MutableLiveData()
    val UserInfo: MutableLiveData<UserInfoResp.UserInfo> = MutableLiveData()

    private fun startCodeCd() {
        viewModelScope.launch {
            var time = 60
            while (time >= 0) {
                CodeCd.postValue(time)
                delay(1000)
                time -= 1
            }
        }
    }

    private fun getCountryCode() {
        ForestClients.Passport.countryList().biliapi(object : RequestCallback<CountryResp.CountryData>() {
            override suspend fun onFailure(code: Int, message: String?) {
                log.warn("country code get failed.")
            }

            override suspend fun onResponse(data: CountryResp.CountryData) {
                CountryData.postValue(data.common.associateBy(
                    keySelector = { it.id },
                ))
            }
        }, viewModelScope)
    }

    fun sendSms(
        cid: Int, tel: Long,
        buvid: String, loginSessionId: String,
        token: String, challenge: String, validate: String, seccode: String,
        onSuccess: (String) -> Unit,
    ) {
        ForestClients.Passport.smsSend(
            cid, tel, loginSessionId, token, challenge, validate, seccode, buvid
        ).biliapi(object : RequestCallback<SmsSendResp.SmsSendData>() {
            override suspend fun onFailure(code: Int, message: String?) {
                Exception.postValue(code, message)
            }

            override suspend fun onResponse(data: SmsSendResp.SmsSendData) {
                startCodeCd()
                onSuccess.invoke(data.captchaKey)
            }
        }, viewModelScope)
    }

    fun getCaptcha() {
        ForestClients.Passport.captcha().biliapi(object : RequestCallback<CaptchaResp.CaptchaData>() {
            override suspend fun onFailure(code: Int, message: String?) {
                Exception.postValue(code, message)
            }

            override suspend fun onResponse(data: CaptchaResp.CaptchaData) {
                CaptchaData.postValue(data)
            }
        }, viewModelScope)
    }


    private val ct: Pattern by lazy { return@lazy Pattern.compile("ct=(.*?)&") }
    private val gt: Pattern by lazy { return@lazy Pattern.compile("gt=(.*?)&") }
    private val challenge: Pattern by lazy { return@lazy Pattern.compile("challenge=(.*?)&") }
    private val token: Pattern by lazy { return@lazy Pattern.compile("hash=(.*?)&") }
    private fun parseCaptcha(obj: JsonObject) {
        val url = obj.getAsJsonObject("data").get("url").asString
        log.debug("验证 URL：$url")
        val ct = ct.matchString("$url&", "ct=&").let {
            return@let it.substring(3, it.length - 1)
        }
        if (ct == "geetest" || ct == "1") {
            val gt = gt.matchString("$url&", "gt=&").let {
                return@let it.substring(3, it.length - 1)
            }
            val challenge = challenge.matchString("$url&", "challenge=&").let {
                return@let it.substring(10, it.length - 1)
            }
            val token = token.matchString("$url&", "token=&").let {
                return@let it.substring(5, it.length - 1)
            }
            CaptchaData.postValue(CaptchaResp.CaptchaData().also {
                it.geetest.gt = gt
                it.geetest.challenge = challenge
                it.token = token
            })
            return
        }
        ForestClients.Passport.captcha().biliapi(object : RequestCallback<CaptchaResp.CaptchaData>() {
            override suspend fun onFailure(code: Int, message: String?) {
                Exception.postValue(code, message)
            }

            override suspend fun onResponse(data: CaptchaResp.CaptchaData) {
                CaptchaData.postValue(data)
            }
        }, viewModelScope)
    }

    private fun loginNextAction(request: ForestRequest<LoginResp>, onPhoneValidate: (String) -> Unit) {
        val data = request.execute(LoginResp::class.java).data
        if (data.status == 2) {
            onPhoneValidate.invoke(data.url)
        } else {
            LoginData.postValue(data)
        }
    }

    fun accessToken(code: String) {
        ForestClients.Passport.accessToken(code).biliapi(object : RequestCallback<LoginResp.LoginData>() {
            override suspend fun onFailure(code: Int, message: String?) {
                Exception.postValue(code, message)
            }

            override suspend fun onResponse(data: LoginResp.LoginData) {
                LoginData.postValue(data)
            }
        }, viewModelScope)
    }

    fun getUserInfo(accessToken: String) {
        ForestClients.App.getUserInfoRequest(accessToken).biliapi(object : RequestCallback<UserInfoResp.UserInfo>() {
            override suspend fun onFailure(code: Int, message: String?) {
                Exception.postValue(code, message)
            }

            override suspend fun onResponse(data: UserInfoResp.UserInfo) {
                UserInfo.postValue(data)
            }
        }, viewModelScope)
    }
}