package io.github.sgpublic.bilidownload.app.activity

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.app.BaseActivity
import io.github.sgpublic.bilidownload.core.exsp.BangumiPreference
import io.github.sgpublic.bilidownload.databinding.ActivitySettingBinding
import io.github.sgpublic.exsp.ExPreference

class Setting: BaseActivity<ActivitySettingBinding>() {
    private var Bangumi: BangumiPreference = ExPreference.get()

    override fun onActivityCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onViewSetup() {
        setSupportActionBar(ViewBinding.settingToolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.title_mine_setting)
        }
        ViewBinding.settingAutoStartBase.alpha = 0.3F
        ViewBinding.settingAutoStartBase.isClickable = false
        ViewBinding.settingAutoStartBase.setOnClickListener {
            ViewBinding.settingAutoStart.isChecked = !ViewBinding.settingAutoStart.isChecked
        }
        ViewBinding.settingAutoStart.setOnCheckedChangeListener { _, isChecked ->
            Bangumi.isTaskAutoStart = isChecked
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }

    override val ViewBinding: ActivitySettingBinding by viewBinding()
    companion object {
        fun startActivity(context: Context){
            val intent = Intent(context, Setting::class.java)
            context.startActivity(intent)
        }
    }
}