package io.github.sgpublic.bilidownload.app.activity

import android.content.Context
import android.content.Intent
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.app.ui.fragment.HomeFragmentAdapter
import io.github.sgpublic.bilidownload.base.app.BaseActivity
import io.github.sgpublic.bilidownload.databinding.ActivityHomeBinding

class Home: BaseActivity<ActivityHomeBinding>() {
    override fun onActivityCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onViewSetup() {
        ViewBinding.homeNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home_nav_bangumi -> {
                    ViewBinding.homePager.setCurrentItem(0, false)
                }
                R.id.home_nav_mine -> {
                    ViewBinding.homePager.setCurrentItem(1, false)
                }
            }
            return@setOnItemSelectedListener true
        }
        ViewBinding.homePager.isUserInputEnabled = false
        ViewBinding.homePager.adapter = HomeFragmentAdapter(this@Home)
    }

    override fun isActivityAtBottom(): Boolean = true
    override val ViewBinding: ActivityHomeBinding by viewBinding()
    companion object {
        fun startActivity(context: Context){
            val intent = Intent(context, Home::class.java)
            context.startActivity(intent)
        }
    }
}