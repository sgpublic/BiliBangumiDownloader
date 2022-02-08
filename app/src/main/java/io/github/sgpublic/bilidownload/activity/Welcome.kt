package io.github.sgpublic.bilidownload.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.BaseActivity
import io.github.sgpublic.bilidownload.base.CrashHandler
import io.github.sgpublic.bilidownload.data.UserData
import io.github.sgpublic.bilidownload.databinding.ActivityWelcomeBinding
import io.github.sgpublic.bilidownload.manager.ConfigManager
import io.github.sgpublic.bilidownload.module.LoginModule
import io.github.sgpublic.bilidownload.module.UpdateModule
import io.github.sgpublic.bilidownload.module.UserInfoModule
import java.util.*

class Welcome: BaseActivity<ActivityWelcomeBinding>(), UpdateModule.Callback {
    private lateinit var activityIntent: Intent

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (!ConfigManager.IS_LOGIN) {
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    ConfigManager.IS_LOGIN = false
                    onSetupFinished()
                }
            }, 500)
            return
        }
        if (ConfigManager.TOKEN_EXPIRE > System.currentTimeMillis()) {
            refreshUserInfo()
            return
        }
        val refreshKey: String = ConfigManager.REFRESH_TOKEN
        val expired = object : TimerTask() {
            override fun run() {
                onToast(R.string.error_login_refresh)
                ConfigManager.IS_LOGIN = false
                onSetupFinished()
            }
        }
        if (refreshKey == "") {
            Timer().schedule(expired, 100)
            return
        }
        val accessToken = ConfigManager.ACCESS_TOKEN
        val helper = LoginModule(this@Welcome)
        helper.refreshToken(accessToken, refreshKey, object : LoginModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                Timer().schedule(expired, 200)
            }

            override fun onLimited() {
                Timer().schedule(expired, 200)
            }

            override fun onSuccess(mid: Long, accessKey: String, refreshKey: String, expiresIn: Long) {
                ConfigManager.MID = mid
                ConfigManager.ACCESS_TOKEN = accessKey
                ConfigManager.REFRESH_TOKEN = refreshKey
                ConfigManager.TOKEN_EXPIRE = expiresIn
                refreshUserInfo()
            }
        })
    }

    override fun onViewSetup() {

    }

    private fun refreshUserInfo(){
        val userInfoModule = UserInfoModule(this@Welcome,
            ConfigManager.ACCESS_TOKEN, ConfigManager.MID)
        userInfoModule.getInfo(object : UserInfoModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                onToast(R.string.error_login)
                ConfigManager.IS_LOGIN = false
                onSetupFinished()
                CrashHandler.saveExplosion(e, code)
            }

            override fun onResult(data: UserData) {
                ConfigManager.NAME = data.name
                ConfigManager.SIGN = data.sign
                ConfigManager.FACE = data.face
                ConfigManager.SEX = data.sex
                ConfigManager.VIP_LABEL = data.vipLabel
                ConfigManager.VIP_TYPE = data.vipType
                ConfigManager.VIP_STATE = data.vipState
                ConfigManager.LEVEL = data.level
                ConfigManager.IS_LOGIN = true
                onSetupFinished()
            }
        })
    }

    private fun onSetupFinished() {
        val helper = UpdateModule(this@Welcome)
        helper.getUpdate(callback = this)

        if (ConfigManager.IS_LOGIN) {
            Home.startActivity(this@Welcome)
        } else {
            Login.startActivity(this@Welcome)
        }
    }

    override fun onUpdate(detailPage: String, isPreRelease: Boolean, dlUrl: String) {
        val builder = AlertDialog.Builder(applicationContext)
        builder.setTitle(R.string.title_update_get)
        builder.setMessage(if (isPreRelease) R.string.text_update_content_dev else
            R.string.text_update_content)
        builder.setPositiveButton(R.string.text_ok) { _, _ ->

        }
        builder.setNegativeButton(R.string.text_cancel) { _, _ ->  }
        runOnUiThread { builder.show() }
    }

    override fun isActivityAtBottom(): Boolean = true
}