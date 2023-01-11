package io.github.sgpublic.bilidownload.app.activity

import android.content.Context
import android.content.Intent
import android.view.View.OnFocusChangeListener
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.lifecycle.viewModelScope
import com.geetest.sdk.GT3ConfigBean
import com.geetest.sdk.GT3ErrorBean
import com.geetest.sdk.GT3GeetestUtils
import com.geetest.sdk.GT3Listener
import com.google.gson.JsonObject
import com.lxj.xpopup.XPopup
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.app.dialog.GeetestDialog
import io.github.sgpublic.bilidownload.app.viewmodel.LoginPwdModel
import io.github.sgpublic.bilidownload.base.app.BaseViewModelActivity
import io.github.sgpublic.bilidownload.core.exsp.TokenPreference
import io.github.sgpublic.bilidownload.core.exsp.UserPreference
import io.github.sgpublic.bilidownload.core.util.fromGson
import io.github.sgpublic.bilidownload.core.util.log
import io.github.sgpublic.bilidownload.core.util.take
import io.github.sgpublic.bilidownload.core.util.takeOr
import io.github.sgpublic.bilidownload.databinding.ActivityLoginPwdBinding
import io.github.sgpublic.exsp.ExPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginPwd: BaseViewModelActivity<ActivityLoginPwdBinding, LoginPwdModel>() {
    private val Token: TokenPreference by lazy { ExPreference.get() }
    private val User: UserPreference by lazy { ExPreference.get() }

    private lateinit var phoneValidate: ActivityResultLauncher<String>
    override fun onActivityCreated(hasSavedInstanceState: Boolean) {
        Token.isLogin = false
        phoneValidate = registerForActivityResult(
            LoginValidateAccount.LoginValidateContract
        ) {
            if (it == null) {
                ViewModel.Loading.postValue(false)
            } else {
                ViewModel.accessToken(it)
            }
        }
    }

    private val geetest: GT3GeetestUtils by lazy {
        return@lazy GT3GeetestUtils(this).also {
            it.init(geetestBean)
        }
    }
    private val geetestBean: GT3ConfigBean by lazy {
        return@lazy GT3ConfigBean().also {
            it.listener = object : GT3Listener() {
                override fun onReceiveCaptchaCode(p0: Int) { }
                override fun onStatistics(p0: String?) { }
                override fun onClosed(p0: Int) { }
                override fun onSuccess(p0: String?) { }
                override fun onFailed(p0: GT3ErrorBean?) {
                    ViewModel.Loading.postValue(false)
                }
                override fun onDialogResult(result: String) {
                    geetest.dismissGeetestDialog()
                    ViewModel.Loading.postValue(true)
                    val obj = JsonObject::class.java.fromGson(result)
                    ViewModel.startGeetestAction(
                        geetestBean.api1Json.getString("token"),
                        obj.get("geetest_challenge").asString,
                        obj.get("geetest_validate").asString,
                        obj.get("geetest_seccode").asString,
                        ViewBinding.loginUsername.editText!!.text.takeOr(""),
                        ViewBinding.loginPassword.editText!!.text.takeOr(""),
                        ::validatePhone
                    )
                }
                override fun onButtonClick() {
                    geetest.getGeetest()
                }
            }
        }
    }
    override fun onViewModelSetup() {
        val asLoading = XPopup.Builder(this).asLoading(
            Application.getString(R.string.text_login_action_doing)
        )
        ViewModel.Loading.observe(this) {
            it.take({asLoading.show()}, {asLoading.dismiss()})
        }
        ViewModel.Exception.observe(this) {
            ViewModel.Loading.postValue(false)
            Application.onToast(this, R.string.text_login_failed, it.message, it.code)
        }
        ViewModel.CaptchaData.observe(this) { data ->
            ViewModel.Loading.postValue(false)
//            geetestBean.api1Json = JSONObject().also {
//                it.put("success", 1)
//                it.put("challenge", data.geetest.challenge)
//                it.put("gt", data.geetest.gt)
//                it.put("token", data.token)
//            }
//            geetest.startCustomFlow()
            XPopup.Builder(this)
                .asCustom(GeetestDialog(this, data.url, {
                    ViewModel.Loading.postValue(false)
                }, { validate ->
                    ViewModel.startGeetestAction(
                        data.token, data.geetest.challenge,
                        validate, "$validate|jordan",
                        ViewBinding.loginUsername.editText!!.text.takeOr(""),
                        ViewBinding.loginPassword.editText!!.text.takeOr(""),
                        ::validatePhone
                    )
                }))
                .show()
        }
        ViewModel.LoginData.observe(this) { data ->
            Token.accessToken = data.tokenInfo.accessToken
            Token.refreshToken = data.tokenInfo.refreshToken
            for (cookie in data.cookieInfo.cookies) {
                when (cookie.name) {
                    "bili_jct" -> Token.cookieBiliJct = cookie.value
                    "DedeUserID" -> Token.cookieDedeUserID = cookie.value
                    "DedeUserID__ckMd5" -> Token.cookieDedeUserID_ckMd5 = cookie.value
                    "sid" -> Token.cookieSid = cookie.value
                    "SESSDATA" -> Token.cookieSESSDATA = cookie.value
                }
            }
            ViewModel.getUserInfo(Token.accessToken)
        }
        ViewModel.UserInfo.observe(this) { data ->
            ViewModel.viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    User.mid = data.mid
                    User.name = data.name
                    User.sign = data.sign
                    User.face = data.face
                    User.sex = data.sex
                    User.level = data.level
                    User.vipStatus = data.vip.status
                    User.vipType = data.vip.type
                    User.vipLabel = data.vip.label.text

                    Token.isLogin = true
                }
                Application.onToast(this@LoginPwd, R.string.text_login_success)
                Application.onToast(this@LoginPwd, User.name)
                Home.startActivity(this@LoginPwd)
                finish()
            }
        }
    }

    private fun validatePhone(url: String) {
        log.debug("校验手机号：$url")
//        val realUrl = when {
//            url.contains("/h5-app/passport/risk/verify") -> {
//                // TODO 这里只是简单做替换，新的验证页需要拦截 POST 请求，未完待续
//                url.replace(
//                    "/h5-app/passport/risk/verify",
//                    "/account/mobile/security/managephone/phone/verify",
//                )
//            }
//            else -> url
//        }
        phoneValidate.launch(url)
    }

    override fun onViewSetup() {
        ViewBinding.loginAction.setOnClickListener {
            val username = ViewBinding.loginUsername.editText!!.text.toString()
            val password = ViewBinding.loginPassword.editText!!.text.toString()
            if (username.isBlank() || password.isBlank()) {
                Application.onToast(this, R.string.text_login_error_empty)
                return@setOnClickListener
            }
            ViewModel.Loading.postValue(true)
            ViewModel.startAction(username, password, ::validatePhone)
        }
        ViewBinding.loginPasswordContent.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    ViewBinding.loginBannerLeft.setImageResource(R.drawable.pic_login_banner_left_hide)
                    ViewBinding.loginBannerRight.setImageResource(R.drawable.pic_login_banner_right_hide)
                } else {
                    ViewBinding.loginBannerLeft.setImageResource(R.drawable.pic_login_banner_left_show)
                    ViewBinding.loginBannerRight.setImageResource(R.drawable.pic_login_banner_right_show)
                }
            }
    }

    override fun onDestroy() {
        ViewModel.Loading.postValue(false)
        geetest.destory()
        super.onDestroy()
    }

    override val ViewBinding: ActivityLoginPwdBinding by viewBinding()
    override val ViewModel: LoginPwdModel by viewModels()
    override fun isActivityAtBottom(): Boolean = true
    companion object {
        fun startActivity(context: Context){
            val intent = Intent().run {
                setClass(context, LoginPwd::class.java)
            }
            context.startActivity(intent)
        }
    }
}