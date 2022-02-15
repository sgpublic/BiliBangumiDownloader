package io.github.sgpublic.bilidownload.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.annotation.StringRes
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.BaseActivity
import io.github.sgpublic.bilidownload.data.UserData
import io.github.sgpublic.bilidownload.databinding.ActivityLoginBinding
import io.github.sgpublic.bilidownload.manager.ConfigManager
import io.github.sgpublic.bilidownload.module.LoginQrcodeModule
import io.github.sgpublic.bilidownload.module.UserInfoModule

class Login: BaseActivity<ActivityLoginBinding>() {
    private val module = LoginQrcodeModule(this@Login)

    override fun onCreateViweBinding(): ActivityLoginBinding =
        ActivityLoginBinding.inflate(layoutInflater)

    override fun onActivityCreated(hasSavedInstanceState: Boolean) {
        ConfigManager.IS_LOGIN = false
        module.setQrcodeOnLoadCallback(object : LoginQrcodeModule.QrcodeLoadCallback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                runOnUiThread {
                    ViewBinding.loginQrcodeNotice.text = getString(
                        R.string.text_login_qrcode_exception,
                        message ?: e?.message ?: "未知"
                    )
                }
                setAnimateState(false, 200, ViewBinding.loginQrcodeLoading)
                setAnimateState(true, 200, ViewBinding.loginQrcodeNotice) {
                    ViewBinding.loginQrcodeNotice.isClickable = true
                }
            }

            override fun onGetStart() {
                ViewBinding.loginQrcodeNotice.isClickable = false
                setAnimateState(true, 200, ViewBinding.loginQrcodeLoading)
                setAnimateState(false, 200, ViewBinding.loginQrcodeNotice)
            }

            override fun onResolve(qrcode: Bitmap) {
                ViewBinding.loginQrcodeNotice.isClickable = false
                setAnimateState(false, 200, ViewBinding.loginQrcodeLoading)
                setAnimateState(false, 200, ViewBinding.loginQrcodeNotice)
                runOnUiThread {
                    ViewBinding.loginQrcodeImage.setImageBitmap(qrcode)
                }
                setAnimateState(true, 200, ViewBinding.loginQrcodeImage)
            }
        })
        module.setQrcodeOnConfirmCallback(object : LoginQrcodeModule.QrcodeConfirmCallback {
            override fun onExpired() {
                setNotice(R.string.text_login_qrcode_expire)
            }

            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                runOnUiThread {
                    ViewBinding.loginQrcodeNotice.text = getString(
                        R.string.text_login_qrcode_exception,
                        message ?: e?.message ?: "未知"
                    )
                }
                setAnimateState(false, 200, ViewBinding.loginQrcodeLoading)
                setAnimateState(true, 200, ViewBinding.loginQrcodeNotice) {
                    ViewBinding.loginQrcodeNotice.isClickable = true
                }
            }

            override fun onScanned() {
                setNotice(R.string.text_login_qrcode_confirm)
            }

            override fun onSuccess(mid: Long, accessKey: String,
                                   refreshKey: String, expiresIn: Long) {
                ViewBinding.loginQrcodeNotice.isClickable = false
                setAnimateState(true, 200, ViewBinding.loginQrcodeLoading)
                setAnimateState(false, 200, ViewBinding.loginQrcodeNotice)
                ConfigManager.MID = mid
                ConfigManager.ACCESS_TOKEN = accessKey
                ConfigManager.REFRESH_TOKEN = refreshKey
                ConfigManager.TOKEN_EXPIRE = expiresIn
                getUserInfo()
            }
        })
        module.getQrcode()
    }

    private val waitingForResume = Object()
    private fun getUserInfo() {
        synchronized(waitingForResume) {
            waitingForResume.wait()
        }
        val module = UserInfoModule(this@Login, ConfigManager.ACCESS_TOKEN, ConfigManager.MID)
        module.getInfo(object : UserInfoModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                runOnUiThread {
                    ViewBinding.loginQrcodeNotice.text = getString(
                        R.string.text_login_info_exception,
                        message ?: e?.message ?: "未知"
                    )
                }
                setAnimateState(false, 200, ViewBinding.loginQrcodeLoading)
                setAnimateState(true, 200, ViewBinding.loginQrcodeNotice) {
                    ViewBinding.loginQrcodeNotice.isClickable = true
                }
            }

            override fun onResult(data: UserData) {
                ConfigManager.saveUserData(data)
                ConfigManager.IS_LOGIN = true
                Application.onToast(this@Login, R.string.text_login_success)
                runOnUiThread {
                    Home.startActivity(this@Login)
                    finish()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        synchronized(waitingForResume) {
            waitingForResume.notify()
        }
    }

    private fun setNotice(@StringRes id: Int) {
        runOnUiThread {
            ViewBinding.loginQrcodeNotice.text = getText(id)
        }
        setAnimateState(false, 200, ViewBinding.loginQrcodeLoading)
        setAnimateState(true, 200, ViewBinding.loginQrcodeNotice) {
            ViewBinding.loginQrcodeNotice.isClickable = true
        }
    }

    override fun onViewSetup() {
        ViewBinding.loginQrcodeNotice.isClickable = false
        ViewBinding.loginQrcodeNotice.setOnClickListener {
            module.getQrcode()
        }
    }

    companion object {
        fun startActivity(context: Context){
            val intent = Intent(context, Login::class.java)
            context.startActivity(intent)
        }
    }

    override fun isActivityAtBottom(): Boolean = true
}