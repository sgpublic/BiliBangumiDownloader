package io.github.sgpublic.bilidownload.activity

import io.github.sgpublic.bilidownload.base.BaseActivity
import io.github.sgpublic.bilidownload.databinding.ActivityDownloadBinding

class TaskList: BaseActivity<ActivityDownloadBinding>() {
    override fun onActivityCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onCreateViweBinding(): ActivityDownloadBinding =
        ActivityDownloadBinding.inflate(layoutInflater)

    override fun onViewSetup() {

    }
}