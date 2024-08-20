package io.github.sgpublic.bilidownload.app.activity

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
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
import io.github.sgpublic.bilidownload.app.ui.list.CountrySpinnerAdapter
import io.github.sgpublic.bilidownload.app.viewmodel.LoginPwdModel
import io.github.sgpublic.bilidownload.app.viewmodel.LoginSmsModel
import io.github.sgpublic.bilidownload.base.app.BaseViewModelActivity
import io.github.sgpublic.bilidownload.core.exsp.TokenPreference
import io.github.sgpublic.bilidownload.core.exsp.UserPreference
import io.github.sgpublic.bilidownload.core.util.fromGson
import io.github.sgpublic.bilidownload.core.util.genBuvid
import io.github.sgpublic.bilidownload.core.util.log
import io.github.sgpublic.bilidownload.core.util.take
import io.github.sgpublic.bilidownload.core.util.takeOr
import io.github.sgpublic.bilidownload.databinding.ActivityLoginPwdBinding
import io.github.sgpublic.bilidownload.databinding.ActivityLoginSmsBinding
import io.github.sgpublic.exsp.ExPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID

class LoginSms: BaseViewModelActivity<ActivityLoginSmsBinding, LoginSmsModel>() {
    private val Token: TokenPreference by lazy { ExPreference.get() }
    private val User: UserPreference by lazy { ExPreference.get() }

    override fun onActivityCreated(hasSavedInstanceState: Boolean) {
        Token.isLogin = false
    }

    private val geetest: GT3GeetestUtils by lazy {
        return@lazy GT3GeetestUtils(this).also {
            it.init(geetestBean)
        }
    }
    private val geetestBean: GT3ConfigBean by lazy {
        return@lazy GT3ConfigBean().also {
            it.isCanceledOnTouchOutside = false
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
                    val buvid = genBuvid()
                    val loginSessionId = UUID.randomUUID().toString().replace("-", "")
                    Token.buvid = buvid
                    Token.loginSessionId = loginSessionId
                    val obj = JsonObject::class.java.fromGson(result)
                    ViewModel.sendSms(
                        cid = ViewModel.CountrySelected,
                        tel = ViewBinding.loginPhone.editText!!.text.takeOr("").toLong(),
                        buvid = buvid,
                        loginSessionId = loginSessionId,
                        token = geetestBean.api1Json.getString("token"),
                        challenge = obj.get("geetest_challenge").asString,
                        validate = obj.get("geetest_validate").asString,
                        seccode = obj.get("geetest_seccode").asString,
                    ) {
                        ViewModel.Loading.postValue(false)
                    }
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
        ViewModel.CodeCd.observe(this) { time ->
            when {
                time < 0 -> {
                    ViewBinding.loginGetCode.setText(R.string.text_login_get_code)
                    ViewBinding.loginGetCode.isEnabled = true
                }
                time == 0 -> {
                    ViewBinding.loginGetCode.setText(R.string.text_login_get_code_retry)
                    ViewBinding.loginGetCode.isEnabled = true
                }
                else -> {
                    ViewBinding.loginGetCode.text = getString(R.string.text_login_get_code_try_later, time)
                    ViewBinding.loginGetCode.isEnabled = false
                }
            }
        }
        ViewModel.CountryData.observe(this) { country ->
            ViewBinding.loginCountry.adapter = CountrySpinnerAdapter(ArrayList(country.entries))
            ViewBinding.loginCountry.setSelection(0)
            ViewBinding.loginCountry.visibility = View.VISIBLE
        }
        ViewModel.Loading.observe(this) {
            it.take({asLoading.show()}, {asLoading.dismiss()})
        }
        ViewModel.Exception.observe(this) {
            ViewModel.Loading.postValue(false)
            Application.onToast(this, R.string.text_login_failed, it.message, it.code)
        }
        ViewModel.CaptchaData.observe(this) { data ->
            ViewModel.Loading.postValue(false)
            log.debug("CaptchaData: $data")
            geetestBean.api1Json = JSONObject().also {
                it.put("success", 1)
                it.put("challenge", data.geetest.challenge)
                it.put("gt", data.geetest.gt)
                it.put("token", data.token)
            }
            geetest.startCustomFlow()
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
                Application.onToast(this@LoginSms, R.string.text_login_success)
                Application.onToast(this@LoginSms, User.name)
                Home.startActivity(this@LoginSms)
                finish()
            }
        }
    }

    override fun onViewSetup() {
        ViewBinding.loginAction.setOnClickListener {
            val code = ViewBinding.loginCode.editText!!.text.toString()
            if (code.isBlank()) {
                Application.onToast(this, R.string.text_login_error_code_empty)
                return@setOnClickListener
            }
            ViewModel.Loading.postValue(true)
//            ViewModel.startAction(phone, code, ::validatePhone)
        }
        ViewBinding.loginCodeContent.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                ViewBinding.loginBannerLeft.setImageResource(R.drawable.pic_login_banner_left_hide)
                ViewBinding.loginBannerRight.setImageResource(R.drawable.pic_login_banner_right_hide)
            } else {
                ViewBinding.loginBannerLeft.setImageResource(R.drawable.pic_login_banner_left_show)
                ViewBinding.loginBannerRight.setImageResource(R.drawable.pic_login_banner_right_show)
            }
        }
        ViewBinding.loginGetCode.setOnClickListener {
            val phone = ViewBinding.loginPhone.editText!!.text.toString()
            if (phone.isBlank()) {
                Application.onToast(this, R.string.text_login_error_phone_empty)
                return@setOnClickListener
            }
            ViewModel.getCaptcha()
        }
        ViewBinding.loginCountry.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                ViewModel.CountrySelected = id.toInt()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }
    }

    override fun onDestroy() {
        ViewModel.Loading.postValue(false)
        geetest.destory()
        super.onDestroy()
    }

    override val ViewBinding: ActivityLoginSmsBinding by viewBinding()
    override val ViewModel: LoginSmsModel by viewModels()
    override fun isActivityAtBottom(): Boolean = true
    companion object {
        fun startActivity(context: Context){
            val intent = Intent().run {
                setClass(context, LoginSms::class.java)
            }
            context.startActivity(intent)
        }
    }
}