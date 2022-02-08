package io.github.sgpublic.bilidownload.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import io.github.sgpublic.bilidownload.BuildConfig
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.BaseActivity
import io.github.sgpublic.bilidownload.base.CrashHandler
import io.github.sgpublic.bilidownload.databinding.ActivityAboutBinding
import io.github.sgpublic.bilidownload.manager.ConfigManager
import io.github.sgpublic.bilidownload.module.UpdateModule

class About: BaseActivity<ActivityAboutBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {

    }

    override fun onViewSetup() {
        setSupportActionBar(binding.aboutToolbar)
        supportActionBar?.run {
            title = ""
            setDisplayHomeAsUpEnabled(true)
        }
        try {
            binding.aboutVersion.text = getString(
                R.string.text_version, BuildConfig.VERSION_NAME
            )
        } catch (ignore: PackageManager.NameNotFoundException) { }
        binding.aboutOpensource.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://github.com/${BuildConfig.GITHUB_REPO}")
            startActivity(intent)
        }
        binding.aboutFeedback.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://github.com/${BuildConfig.GITHUB_REPO}/issues")
            startActivity(intent)
        }
        binding.aboutLicense.setOnClickListener {
            License.startActivity(this@About)
        }
        binding.aboutUpdate.setOnClickListener {
            onUpdate()
        }
    }

    private fun onUpdate() {
        binding.aboutProgress.visibility = View.VISIBLE
        val helper = UpdateModule(this@About)
        helper.getUpdate(ConfigManager.UPDATE_CHANNEL, object : UpdateModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable) {
                runOnUiThread {
                    onToast(R.string.error_update, code)
                    binding.aboutProgress.visibility = View.GONE
                    CrashHandler.saveExplosion(e, code)
                }
            }

            override fun onUpToDate() {
                runOnUiThread {
                    binding.aboutProgress.visibility = View.GONE
                    onToast(R.string.title_update_already)
                }
            }

            override fun onUpdate(detailPage: String, isPreRelease: Boolean, dlUrl: String) {
                val builder = AlertDialog.Builder(applicationContext)
                builder.setTitle(R.string.title_update_get)
                builder.setMessage(if (isPreRelease) R.string.text_update_content_dev else
                    R.string.text_update_content)
                builder.setPositiveButton(R.string.text_ok) { _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(detailPage)
                    startActivity(intent)
                }
                builder.setNegativeButton(R.string.text_cancel) { _, _ ->  }
                runOnUiThread {
                    binding.aboutProgress.visibility = View.GONE
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