package io.github.sgpublic.bilidownload.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.BaseActivity
import io.github.sgpublic.bilidownload.databinding.ActivityHomeBinding
import io.github.sgpublic.bilidownload.ui.fragment.HomeFragmentAdapter

class Home: BaseActivity<ActivityHomeBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        binding.navBangumi.callOnClick()
    }

    override fun onViewSetup() {
        binding.navBangumi.setOnClickListener {
            binding.homePager.setCurrentItem(0, false)
            selectNavigation(0)
        }
        binding.navMine.setOnClickListener {
            binding.homePager.setCurrentItem(1, false)
            selectNavigation(1)
        }
        binding.homePager.isUserInputEnabled = false
        binding.homePager.adapter = HomeFragmentAdapter(this@Home)
        initViewAtBottom(binding.navView)
    }

    private fun selectNavigation(index: Int){
        binding.navBangumiImage.setColorFilter(getSelectedColor(index == 0))
        binding.navBangumiTitle.setTextColor(getSelectedColor(index == 0))
        binding.navMineImage.setColorFilter(getSelectedColor(index == 1))
        binding.navMineTitle.setTextColor(getSelectedColor(index == 1))
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
        binding.navView.setFPS(0)
    }

    override fun onResume() {
        super.onResume()
        binding.navView.setFPS(60)
    }

    override fun isActivityAtBottom(): Boolean = true

    companion object {
        fun startActivity(context: Context){
            val intent = Intent(context, Home::class.java)
            context.startActivity(intent)
        }
    }
}