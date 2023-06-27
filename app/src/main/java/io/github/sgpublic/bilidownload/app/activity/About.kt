package io.github.sgpublic.bilidownload.app.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import io.github.sgpublic.bilidownload.BuildConfig
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.app.BaseActivity
import io.github.sgpublic.bilidownload.core.util.*
import io.github.sgpublic.bilidownload.databinding.ActivityAboutBinding

class About: BaseActivity<ActivityAboutBinding>() {
    override fun onActivityCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onViewSetup() {
        setSupportActionBar(ViewBinding.aboutToolbar)
        supportActionBar?.run {
            title = ""
            setDisplayHomeAsUpEnabled(true)
        }
        try {
            ViewBinding.aboutVersion.text = getString(
                R.string.text_version, BuildConfig.VERSION_NAME
            )
        } catch (ignore: PackageManager.NameNotFoundException) { }
        ViewBinding.aboutOpensource.setOnClickListener {
            IntentUtil.openUrl("https://github.com/${BuildConfig.GITHUB_REPO}")
        }
        ViewBinding.aboutFeedback.setOnClickListener {
            IntentUtil.openUrl("https://github.com/${BuildConfig.GITHUB_REPO}/issues")
        }
        ViewBinding.aboutLicense.setOnClickListener {
            License.startActivity(this@About)
        }
    }

    override val ViewBinding: ActivityAboutBinding by viewBinding()

    companion object {
        fun startActivity(context: Context){
            val intent = Intent(context, About::class.java)
            context.startActivity(intent)
        }
    }
}