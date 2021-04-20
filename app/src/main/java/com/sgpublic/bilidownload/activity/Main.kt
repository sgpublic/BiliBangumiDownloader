package com.sgpublic.bilidownload.activity

import android.os.Bundle
import com.sgpublic.bilidownload.base.BaseActivity
import com.sgpublic.bilidownload.databinding.ActivityMainBinding

class Main: BaseActivity<ActivityMainBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {

    }

    override fun getContentView(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)

    override fun onSetSwipeBackEnable(): Boolean = false
}