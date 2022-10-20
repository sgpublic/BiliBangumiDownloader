package io.github.sgpublic.bilidownload.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dtflys.forest.http.ForestRequest
import com.google.gson.JsonObject
import io.github.sgpublic.bilidownload.base.app.BaseViewModel
import io.github.sgpublic.bilidownload.base.app.postValue
import io.github.sgpublic.bilidownload.core.forest.core.BiliApiException
import io.github.sgpublic.bilidownload.core.forest.data.CaptchaResp
import io.github.sgpublic.bilidownload.core.forest.data.GetKeyResp
import io.github.sgpublic.bilidownload.core.forest.data.LoginResp
import io.github.sgpublic.bilidownload.core.forest.data.UserInfoResp
import io.github.sgpublic.bilidownload.core.util.*
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*
import java.util.regex.Pattern
import javax.crypto.Cipher

class LoginPwdModel: BaseViewModel() {
    val CaptchaData: MutableLiveData<CaptchaResp.CaptchaData> = MutableLiveData()
    val LoginData: MutableLiveData<LoginResp.LoginData> = MutableLiveData()
    val UserInfo: MutableLiveData<UserInfoResp.UserInfo> = MutableLiveData()

    private fun encryptPwd(password: String, callback: (String) -> Unit) {
        ForestClients.PASSPORT.pubKey().biliapi(object : ForestCallback<GetKeyResp.GetKeyData>() {
            override fun onFailure(code: Int, message: String?) {
                EXCEPTION.postValue(code, message)
            }

            override fun onResponse(data: GetKeyResp.GetKeyData) {
                val pwdEncrypt = try {
                    val pubKey = data.key.replace("\n", "")
                        .substring(26, 242)
                    val keySpec = X509EncodedKeySpec(Base64.getDecoder().decode(pubKey))
                    val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
                    val key: PublicKey = keyFactory.generatePublic(keySpec)
                    val cp: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
                    cp.init(Cipher.ENCRYPT_MODE, key)
                    cp.doFinal((data.hash + password).toByteArray()).BASE_64
                } catch (e: Exception) {
                    log.error("密码加密失败", e)
                    EXCEPTION.postValue(-125, e.message)
                    return
                }
                callback(pwdEncrypt)
            }
        }, viewModelScope)
    }

    fun startAction(username: String, password: String, onPhoneValidate: (String) -> Unit) {
        encryptPwd(password) { pwdEncrypt ->
            try {
                loginNextAction(ForestClients.PASSPORT.login(
                    username, pwdEncrypt
                ), onPhoneValidate)
            } catch (e: BiliApiException) {
                if (e.code != -105) {
                    log.error("登陆失败", e)
                    EXCEPTION.postValue(e)
                    return@encryptPwd
                }
                log.warn("登陆需要验证码", e)
                parseCaptcha(e.body)
            }
        }
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
                it.url = url
            })
            return
        }
        ForestClients.PASSPORT.captcha().biliapi(object :
            ForestCallback<CaptchaResp.CaptchaData>() {
            override fun onFailure(code: Int, message: String?) {
                EXCEPTION.postValue(code, message)
            }

            override fun onResponse(data: CaptchaResp.CaptchaData) {
                CaptchaData.postValue(data)
            }
        }, viewModelScope)
    }

    fun startGeetestAction(
        token: String, challenge: String, validate: String,
        seccode: String, username: String, password: String,
        onPhoneValidate: (String) -> Unit
    ) {
        encryptPwd(password) { pwdEncrypt ->
            try {
                loginNextAction(ForestClients.PASSPORT.geetestLogin(
                    token, challenge, validate, seccode, username, pwdEncrypt
                ), onPhoneValidate)
            } catch (e: BiliApiException) {
                log.error("登陆失败", e)
                EXCEPTION.postValue(e)
            }
        }
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
        ForestClients.PASSPORT.accessToken(code).biliapi(object : ForestCallback<LoginResp.LoginData>() {
            override fun onFailure(code: Int, message: String?) {
                EXCEPTION.postValue(code, message)
            }

            override fun onResponse(data: LoginResp.LoginData) {
                LoginData.postValue(data)
            }
        }, viewModelScope)
    }

    fun getUserInfo(accessToken: String) {
        ForestClients.APP.getUserInfoRequest(accessToken).biliapi(object : ForestCallback<UserInfoResp.UserInfo>() {
            override fun onFailure(code: Int, message: String?) {
                EXCEPTION.postValue(code, message)
            }

            override fun onResponse(data: UserInfoResp.UserInfo) {
                UserInfo.postValue(data)
            }
        }, viewModelScope)
    }
}