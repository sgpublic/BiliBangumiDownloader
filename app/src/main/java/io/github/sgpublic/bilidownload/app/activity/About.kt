package io.github.sgpublic.bilidownload.app.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonObject
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.BuildConfig
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.app.BaseActivity
import io.github.sgpublic.bilidownload.core.exsp.UpdatePreference
import io.github.sgpublic.bilidownload.core.util.*
import io.github.sgpublic.bilidownload.databinding.ActivityAboutBinding
import io.github.sgpublic.exsp.ExPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            io.github.sgpublic.bilidownload.app.activity.License.Companion.startActivity(this@About)
        }
        ViewBinding.aboutUpdate.setOnClickListener {
            update()
        }
    }

    private val Update: UpdatePreference by lazy { ExPreference.get() }
    private fun update() {
        ViewBinding.aboutProgress.visibility = View.VISIBLE
        ForestClients.Github.getRelease().enqueue(object : RequestCallback<JsonObject>() {
            override fun onFailure(code: Int, message: String?) {
                log.warn("Update check failed: ${message ?: "Unknown reason."}")
                onUpToDate()
            }

            override fun onResponse(data: JsonObject) {
                if (data.has("message")) {
                    onUpToDate()
                    return
                }
                if (data.get("draft").asBoolean || data.get("prerelease").asBoolean) {
                    onUpToDate()
                    return
                }
                val version = data.get("name").asString
                if (Update.updated == version) {
                    onUpToDate()
                    return
                }
                val originVer = version.split("-")[0]
                if (!originVer.isHigherThanCurrent() && originVer != BuildConfig.ORIGIN_VERSION_NAME) {
                    onUpToDate()
                    return
                }
                for (element in data.get("assets").asJsonArray) {
                    val asset = element.asJsonObject
                    if (asset.get("content_type").asString != "application/vnd.android.package-archive") {
                        continue
                    }
                    onUpdate(
                        version,
                        data.get("body").asString,
                        asset.get("browser_download_url").asString
                    )
                    return
                }
            }

            private fun onUpToDate() {
                lifecycleScope.launch {
                    withContext(Dispatchers.Main) {
                        ViewBinding.aboutProgress.visibility = View.GONE
                        Application.onToast(this@About, R.string.title_update_already)
                    }
                }
            }
        }, lifecycleScope)
    }

    private fun onUpdate(version: String, changeLog: String, asset: String) {
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.title_update_get)
            .setMessage(getString(R.string.text_update_content, version, changeLog))
            .setPositiveButton(R.string.title_update_goto_download) { _, _ ->
                IntentUtil.openUrl(asset)
            }
            .setNegativeButton(R.string.text_cancel) { _, _ -> }
            .setNeutralButton(R.string.title_update_do_not_notice) { _, _ ->
                Update.updated = version
            }
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                dialog.show()
            }
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