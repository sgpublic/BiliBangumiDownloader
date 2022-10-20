package io.github.sgpublic.bilidownload.app.activity

import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.app.BaseActivity
import io.github.sgpublic.bilidownload.core.exsp.TokenPreference
import io.github.sgpublic.bilidownload.core.exsp.UpdatePreference
import io.github.sgpublic.bilidownload.core.exsp.UserPreference
import io.github.sgpublic.bilidownload.databinding.ActivityWelcomeBinding
import io.github.sgpublic.exsp.ExPreference
import java.util.*

class Welcome: BaseActivity<ActivityWelcomeBinding>() {
    private val Token: TokenPreference by lazy { ExPreference.get() }
    private val User: UserPreference by lazy { ExPreference.get() }
    private val Update: UpdatePreference by lazy { ExPreference.get() }

    override fun onActivityCreated(hasSavedInstanceState: Boolean) {
        if (!Token.isLogin) {
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    onSetupFinished()
                }
            }, 500)
            return
        }

        object : TimerTask() {
            override fun run() {
                Application.onToast(this@Welcome, R.string.error_login_refresh)
                Token.isLogin = false
                onSetupFinished()
            }
        }.let { expired ->
            Timer().schedule(expired, 100)
        }
    }

    private fun onSetupFinished() {
        if (Update.needUpdate()) {
            Update.doUpdate()

        }

        if (!Token.isLogin) {
            LoginPwd.startActivity(this@Welcome)
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

    override val ViewBinding: ActivityWelcomeBinding by viewBinding()
    override fun isActivityAtBottom(): Boolean = true
}
