package io.github.sgpublic.bilidownload.activity

import android.content.Context
import android.content.Intent
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.BaseActivity
import io.github.sgpublic.bilidownload.databinding.ActivityHomeBinding
import io.github.sgpublic.bilidownload.ui.fragment.HomeFragmentAdapter

class Home: BaseActivity<ActivityHomeBinding>() {
    override fun onActivityCreated(hasSavedInstanceState: Boolean) {
        ViewBinding.navBangumi.callOnClick()
    }

    override fun onCreateViewBinding(): ActivityHomeBinding =
        ActivityHomeBinding.inflate(layoutInflater)

    override fun onViewSetup() {
        ViewBinding.navBangumi.setOnClickListener {
            ViewBinding.homePager.setCurrentItem(0, false)
            selectNavigation(0)
        }
        ViewBinding.navMine.setOnClickListener {
            ViewBinding.homePager.setCurrentItem(1, false)
            selectNavigation(1)
        }
        ViewBinding.homePager.isUserInputEnabled = false
        ViewBinding.homePager.adapter = HomeFragmentAdapter(this@Home)
        initViewAtBottom(ViewBinding.navView)
    }

    private fun selectNavigation(index: Int){
        ViewBinding.navBangumiImage.setColorFilter(getSelectedColor(index == 0))
        ViewBinding.navBangumiTitle.setTextColor(getSelectedColor(index == 0))
        ViewBinding.navMineImage.setColorFilter(getSelectedColor(index == 1))
        ViewBinding.navMineTitle.setTextColor(getSelectedColor(index == 1))
    }

    private fun getSelectedColor(isSelected: Boolean): Int {
        return if (isSelected) {
            getColor(R.color.colorPrimary)
        } else {
            getColor(R.color.color_text_dark)
        }
    }

    override fun onPause() {
        super.onPause()
        ViewBinding.navView.setFPS(0)
    }

    override fun onResume() {
        super.onResume()
        ViewBinding.navView.setFPS(60)
    }

    override fun isActivityAtBottom(): Boolean = true

    companion object {
        fun startActivity(context: Context){
            val intent = Intent(context, Home::class.java)
            context.startActivity(intent)
        }
    }
}