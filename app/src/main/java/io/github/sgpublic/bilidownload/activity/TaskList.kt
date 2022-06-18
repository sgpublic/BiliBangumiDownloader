package io.github.sgpublic.bilidownload.activity

import android.content.Context
import android.content.Intent
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.BaseActivity
import io.github.sgpublic.bilidownload.databinding.ActivityDownloadBinding
import io.github.sgpublic.bilidownload.fragment.factory.DownloadFragmentFactory

class TaskList: BaseActivity<ActivityDownloadBinding>() {
    override fun onActivityCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onViewSetup() {
        setSupportActionBar(ViewBinding.downloadToolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.text_download)
        }
    }

    override fun onCreateViewBinding(): ActivityDownloadBinding =
        ActivityDownloadBinding.inflate(layoutInflater)

    override fun beforeCreate() {
        supportFragmentManager.fragmentFactory = DownloadFragmentFactory(this@TaskList)
    }

    companion object {
        fun startActivity(context: Context){
            val intent = Intent(context, TaskList::class.java)
            context.startActivity(intent)
        }
    }
}