package io.github.sgpublic.bilidownload.activity

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.BaseActivity
import io.github.sgpublic.bilidownload.databinding.ActivitySettingBinding
import io.github.sgpublic.bilidownload.databinding.DialogSettingTaskCountBinding
import io.github.sgpublic.bilidownload.core.manager.ConfigManager

class Setting: BaseActivity<ActivitySettingBinding>() {
    override fun onActivityCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onCreateViewBinding(): ActivitySettingBinding =
        ActivitySettingBinding.inflate(layoutInflater)

    override fun onViewSetup() {
        setSupportActionBar(ViewBinding.settingToolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.title_mine_setting)
        }

        ViewBinding.settingTaskCount.alpha = 0.3F
        ViewBinding.settingTaskCount.isClickable = false
        ViewBinding.settingTaskCount.setOnClickListener {
            taskCountSetting(1, 3)
        }
        ViewBinding.settingAutoStartBase.alpha = 0.3F
        ViewBinding.settingAutoStartBase.isClickable = false
        ViewBinding.settingAutoStartBase.setOnClickListener {
            ViewBinding.settingAutoStart.isChecked = !ViewBinding.settingAutoStart.isChecked
        }
        ViewBinding.settingAutoStart.setOnCheckedChangeListener { _, isChecked ->
            ConfigManager.TASK_AUTO_START = isChecked
        }
    }

    @Suppress("SameParameterValue")
    private fun taskCountSetting(min: Int, max: Int) {
        var taskCount: Int = ConfigManager.TASK_PARALLEL_COUNT
        val dialogSettingTaskCount = DialogSettingTaskCountBinding.inflate(layoutInflater)
        dialogSettingTaskCount.dialogSettingTaskMin.text = min.toString()
        dialogSettingTaskCount.dialogSettingTaskMax.text = max.toString()
        dialogSettingTaskCount.dialogSettingTaskSeek.min = min
        dialogSettingTaskCount.dialogSettingTaskSeek.max = max
        dialogSettingTaskCount.dialogSettingTaskSeek.progress = taskCount
        dialogSettingTaskCount.dialogSettingTaskSeek.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                taskCount = progress
                dialogSettingTaskCount.dialogSettingTaskShow.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        dialogSettingTaskCount.dialogSettingTaskShow.text = taskCount.toString()

        AlertDialog.Builder(this@Setting)
            .setTitle(R.string.title_setting_task)
            .setView(dialogSettingTaskCount.root)
            .setPositiveButton(R.string.text_ok) { _, _ ->
                ConfigManager.TASK_PARALLEL_COUNT = taskCount
                taskCountLoad()
            }
            .setNegativeButton(R.string.text_cancel, null)
            .show()
    }

    private fun taskCountLoad() {
        ViewBinding.settingTaskCountString.text = String.format(
            getString(R.string.text_setting_task_show),
            ConfigManager.TASK_PARALLEL_COUNT
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }

    companion object {
        fun startActivity(context: Context){
            val intent = Intent(context, Setting::class.java)
            context.startActivity(intent)
        }
    }

}