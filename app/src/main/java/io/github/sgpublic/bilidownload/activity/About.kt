package io.github.sgpublic.bilidownload.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.MenuItem
import android.view.View
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.BuildConfig
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.BaseActivity
import io.github.sgpublic.bilidownload.databinding.ActivityAboutBinding
import io.github.sgpublic.bilidownload.module.UpdateModule
import io.github.sgpublic.bilidownload.util.IntentUtil

class About: BaseActivity<ActivityAboutBinding>() {
    override fun onActivityCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onCreateViweBinding(): ActivityAboutBinding =
        ActivityAboutBinding.inflate(layoutInflater)

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
            IntentUtil.openBrowser("https://github.com/${BuildConfig.GITHUB_REPO}")
        }
        ViewBinding.aboutFeedback.setOnClickListener {
            IntentUtil.openBrowser("https://github.com/${BuildConfig.GITHUB_REPO}/issues")
        }
        ViewBinding.aboutLicense.setOnClickListener {
            License.startActivity(this@About)
        }
        ViewBinding.aboutUpdate.setOnClickListener {
            onUpdate()
        }
    }

    private fun onUpdate() {
        ViewBinding.aboutProgress.visibility = View.VISIBLE
        val helper = UpdateModule(this@About)
        helper.getUpdate(object : UpdateModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                Application.onToast(this@About, R.string.error_update, code)
                runOnUiThread {
                    ViewBinding.aboutProgress.visibility = View.GONE
                }
            }

            override fun onUpToDate() {
                Application.onToast(this@About, R.string.title_update_already)
                runOnUiThread {
                    ViewBinding.aboutProgress.visibility = View.GONE
                }
            }

            override fun onUpdate(isPreRelease: Boolean, remoteVer: Int, detail: String, url: String) {
                val builder = AlertDialog.Builder(applicationContext)
                builder.setTitle(R.string.title_update_get)
                builder.setMessage(
                    "${if (isPreRelease) R.string.text_update_content_dev else
                        R.string.text_update_content}：\n$detail"
                )
                builder.setPositiveButton(R.string.title_update_goto_download) { _, _ ->
                    IntentUtil.openBrowser(url)
                }
                builder.setNegativeButton(R.string.text_cancel) { _, _ ->  }
                runOnUiThread {
                    ViewBinding.aboutProgress.visibility = View.GONE
                    builder.show()
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }

    companion object {
        fun startActivity(context: Context){
            val intent = Intent(context, About::class.java)
            context.startActivity(intent)
        }
    }

}