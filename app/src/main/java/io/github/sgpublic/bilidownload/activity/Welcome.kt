package io.github.sgpublic.bilidownload.activity

import android.content.Intent
import android.net.Uri
import com.lxj.xpopup.XPopup
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.BaseActivity
import io.github.sgpublic.bilidownload.core.data.UserData
import io.github.sgpublic.bilidownload.core.manager.ConfigManager
import io.github.sgpublic.bilidownload.core.module.LoginModule
import io.github.sgpublic.bilidownload.core.module.UpdateModule
import io.github.sgpublic.bilidownload.core.module.UserInfoModule
import io.github.sgpublic.bilidownload.databinding.ActivityWelcomeBinding
import io.github.sgpublic.bilidownload.ui.asConfirm
import java.util.*

class Welcome: BaseActivity<ActivityWelcomeBinding>(), UpdateModule.Callback {
    override fun onCreateViewBinding(): ActivityWelcomeBinding =
        ActivityWelcomeBinding.inflate(layoutInflater)

    override fun onActivityCreated(hasSavedInstanceState: Boolean) {
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
                Application.onToast(this@Welcome, R.string.error_login_refresh)
                ConfigManager.IS_LOGIN = false
                onSetupFinished()
            }
        }
        if (refreshKey == "") {
            Timer().schedule(expired, 100)
            return
        }
        val accessToken = ConfigManager.ACCESS_TOKEN
        val helper = LoginModule()
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

    private fun refreshUserInfo(){
        val userInfoModule = UserInfoModule(
            ConfigManager.ACCESS_TOKEN,
            ConfigManager.MID
        )
        userInfoModule.getInfo(object : UserInfoModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                Application.onToast(this@Welcome, R.string.error_login)
                ConfigManager.IS_LOGIN = false
                onSetupFinished()
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
        if (ConfigManager.needAutoCheckUpdate()) {
            ConfigManager.onUpdate()
            val helper = UpdateModule()
            helper.getUpdate(callback = this)
        }

        if (!ConfigManager.IS_LOGIN) {
            Login.startActivity(this@Welcome)
            return
        }
        if (intent.data == null) {
            Home.startActivity(this@Welcome)
            return
        }
        val sid = intent.data?.path?.substring(1)?.toLongOrNull() ?: return
        val index = intent.data?.getQueryParameter("index")?.toIntOrNull() ?: 0
        SeasonPlayer.startActivity(this@Welcome, sid, index)
    }

    override fun onUpdate(isPreRelease: Boolean, remoteVer: Int, detail: String, url: String) {
        val dialog = XPopup.Builder(this@Welcome).asConfirm(
            R.string.title_update_get,
            "${if (isPreRelease) R.string.text_update_content_dev else
                R.string.text_update_content}：\n$detail"
        ) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
        runOnUiThread {
            dialog.show()
        }
    }

    override fun isActivityAtBottom(): Boolean = true
}
