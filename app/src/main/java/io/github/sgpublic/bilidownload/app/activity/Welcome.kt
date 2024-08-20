package io.github.sgpublic.bilidownload.app.activity

import android.os.Handler
import io.github.sgpublic.bilidownload.base.app.BaseActivity
import io.github.sgpublic.bilidownload.core.exsp.TokenPreference
import io.github.sgpublic.bilidownload.databinding.ActivityWelcomeBinding
import io.github.sgpublic.exsp.ExPreference

class Welcome: BaseActivity<ActivityWelcomeBinding>() {
    private val Token: TokenPreference by lazy { ExPreference.get() }

    override fun onActivityCreated(hasSavedInstanceState: Boolean) {
        Handler(mainLooper).postDelayed({
            onSetupFinish()
        }, 500)
    }

    private fun onSetupFinish() {
        if (!Token.isLogin) {
            LoginQrcode.startActivity(this@Welcome)
            return
        }
        val data = intent.data
        if (data == null) {
            Home.startActivity(this@Welcome)
            return
        }
        val sid = data.path?.substring(1)?.toLongOrNull()
        if (sid == null) {
            finish()
            return
        }
        val epid = data.getQueryParameter("epid")?.toLongOrNull() ?: 0
        SeasonPlayer.startActivity(this@Welcome, sid, epid)
    }

    override val ViewBinding: ActivityWelcomeBinding by viewBinding()
    override fun isActivityAtBottom(): Boolean = true
}
