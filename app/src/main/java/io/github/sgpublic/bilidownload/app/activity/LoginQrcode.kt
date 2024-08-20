package io.github.sgpublic.bilidownload.app.activity

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.geetest.sdk.GT3ConfigBean
import com.geetest.sdk.GT3ErrorBean
import com.geetest.sdk.GT3GeetestUtils
import com.geetest.sdk.GT3Listener
import com.google.gson.JsonObject
import com.lxj.xpopup.XPopup
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.BuildConfig
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.app.ui.list.CountrySpinnerAdapter
import io.github.sgpublic.bilidownload.app.viewmodel.LoginQrcodeModel
import io.github.sgpublic.bilidownload.app.viewmodel.LoginSmsModel
import io.github.sgpublic.bilidownload.base.app.BaseViewModelActivity
import io.github.sgpublic.bilidownload.core.exsp.TokenPreference
import io.github.sgpublic.bilidownload.core.exsp.UserPreference
import io.github.sgpublic.bilidownload.core.util.createQRCodeBitmap
import io.github.sgpublic.bilidownload.core.util.fromGson
import io.github.sgpublic.bilidownload.core.util.genBuvid
import io.github.sgpublic.bilidownload.core.util.log
import io.github.sgpublic.bilidownload.core.util.take
import io.github.sgpublic.bilidownload.core.util.takeOr
import io.github.sgpublic.bilidownload.databinding.ActivityLoginQrcodeBinding
import io.github.sgpublic.bilidownload.databinding.ActivityLoginSmsBinding
import io.github.sgpublic.exsp.ExPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.util.UUID
import kotlin.math.log

class LoginQrcode: BaseViewModelActivity<ActivityLoginQrcodeBinding, LoginQrcodeModel>() {
    private val Token: TokenPreference by lazy { ExPreference.get() }
    private val User: UserPreference by lazy { ExPreference.get() }

    override fun onActivityCreated(hasSavedInstanceState: Boolean) {
        Token.isLogin = false

        lifecycleScope.launch(Dispatchers.Default) {
            delay(500)
            withContext(Dispatchers.Main) {
                ViewModel.getQrcode(
                    ViewBinding.loginQrcodeImage.width,
                    ViewBinding.loginQrcodeImage.height,
                )
            }
        }
    }

    override fun onViewModelSetup() {
        ViewModel.Loading.observe(this) {
            ViewBinding.loginQrcodeLoading.visibility = if (it) View.VISIBLE else View.GONE
        }
        ViewModel.Exception.observe(this) {
            ViewModel.Loading.postValue(false)
            Application.onToast(this, R.string.text_login_failed, it.message, it.code)
        }
        ViewModel.Qrcode.observe(this) {
            ViewModel.Loading.postValue(false)
            ViewBinding.loginQrcodeImage.setImageBitmap(it)
        }
        ViewModel.QrcodeState.observe(this) { state ->
            when (state) {
                LoginQrcodeModel.QrcodeStateEnum.Success -> {
                    ViewBinding.loginQrcodeLoadingBase.visibility = View.VISIBLE
                    ViewModel.Loading.postValue(true)
                    ViewBinding.loginQrcodeNotice.setText(R.string.text_login_action_doing)
                }
                LoginQrcodeModel.QrcodeStateEnum.Error -> {
                    ViewBinding.loginQrcodeLoadingBase.visibility = View.VISIBLE
                    ViewModel.Loading.postValue(false)
                    ViewBinding.loginQrcodeNotice.setText(R.string.text_login_qrcode_expire)
                }
                LoginQrcodeModel.QrcodeStateEnum.Waiting -> {
                    ViewModel.Loading.postValue(false)
                    ViewBinding.loginQrcodeLoadingBase.visibility = View.GONE
                }
                LoginQrcodeModel.QrcodeStateEnum.Scanned -> {
                    ViewBinding.loginQrcodeLoadingBase.visibility = View.VISIBLE
                    ViewModel.Loading.postValue(false)
                    ViewBinding.loginQrcodeNotice.setText(R.string.text_login_qrcode_confirm)
                }
                LoginQrcodeModel.QrcodeStateEnum.Expired -> {
                    ViewBinding.loginQrcodeLoadingBase.visibility = View.VISIBLE
                    ViewModel.Loading.postValue(false)
                    ViewBinding.loginQrcodeNotice.setText(R.string.text_login_qrcode_expire)
                }
            }
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
                Application.onToast(this@LoginQrcode, R.string.text_login_success)
                Application.onToast(this@LoginQrcode, User.name)
                Home.startActivity(this@LoginQrcode)
                finish()
            }
        }
    }

    override fun onViewSetup() {
        ViewBinding.loginQrcodeStart.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val intent = packageManager.getLaunchIntentForPackage("tv.danmaku.bili")
                if (intent != null) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    withContext(Dispatchers.Main) {
                        startActivity(intent)
                    }
                } else {
                    Application.onToast(this@LoginQrcode, R.string.text_login_qrcode_save_failed)
                }
                if (!writeQrcodeToDownload()) {
                    Application.onToast(this@LoginQrcode, R.string.text_login_qrcode_save_failed)
                } else {
                    Application.onToast(this@LoginQrcode, R.string.text_login_qrcode_save_success)
                }
            }
        }
    }

    override fun onDestroy() {
        ViewModel.Loading.postValue(false)
        super.onDestroy()
    }

    private fun writeQrcodeToDownload(): Boolean {
        val bitmap = ViewModel.Qrcode.value
        if (bitmap == null) {
            log.warn("no qrcode to save.")
            return false
        }

        val filename = "login-qrcode-${System.currentTimeMillis()}.png"
        val subdirectory = BuildConfig.PROJECT_NAME

        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, filename)
            put(MediaStore.Downloads.MIME_TYPE, "image/png")
            put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$subdirectory")
        }

        val url = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        if (url == null) {
            log.warn("contentResolver returns null when insert qrcode to download.")
            return false
        }
        contentResolver.openOutputStream(url)?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            return true
        }
        log.warn("contentResolver returns null when openOutputStream.")
        return false
    }

    override val ViewBinding: ActivityLoginQrcodeBinding by viewBinding()
    override val ViewModel: LoginQrcodeModel by viewModels()
    override fun isActivityAtBottom(): Boolean = true
    companion object {
        fun startActivity(context: Context){
            val intent = Intent().run {
                setClass(context, LoginQrcode::class.java)
            }
            context.startActivity(intent)
        }
    }
}