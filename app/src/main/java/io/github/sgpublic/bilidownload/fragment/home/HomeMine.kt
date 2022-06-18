package io.github.sgpublic.bilidownload.fragment.home

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.lxj.xpopup.XPopup
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.activity.*
import io.github.sgpublic.bilidownload.base.BaseFragment
import io.github.sgpublic.bilidownload.core.manager.ConfigManager
import io.github.sgpublic.bilidownload.databinding.FragmentHomeMineBinding
import io.github.sgpublic.bilidownload.ui.asConfirm
import io.github.sgpublic.bilidownload.ui.customLoad
import io.github.sgpublic.bilidownload.ui.withCrossFade

class HomeMine(context: AppCompatActivity): BaseFragment<FragmentHomeMineBinding>(context) {
    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onCreateViewBinding(container: ViewGroup?): FragmentHomeMineBinding =
        FragmentHomeMineBinding.inflate(layoutInflater, container, false)

    override fun onViewSetup() {
        initViewAtTop(ViewBinding.mineToolbar)
        Glide.with(context)
            .customLoad(ConfigManager.FACE)
            .withCrossFade()
            .into(ViewBinding.mineAvatar)
        val vipType = ConfigManager.VIP_TYPE
        if (vipType == 0){
            ViewBinding.mineVip.visibility = View.GONE
            ViewBinding.mineVipString.visibility = View.GONE
        } else {
            ViewBinding.mineVip.visibility = View.VISIBLE
            ViewBinding.mineVipString.visibility = View.VISIBLE
            ViewBinding.mineVipString.text = ConfigManager.VIP_LABEL
        }
        val imageLevels = intArrayOf(
            R.drawable.ic_level_0,
            R.drawable.ic_level_1,
            R.drawable.ic_level_2,
            R.drawable.ic_level_3,
            R.drawable.ic_level_4,
            R.drawable.ic_level_5,
            R.drawable.ic_level_6
        )
        ViewBinding.mineLevel.setImageResource(imageLevels[ConfigManager.LEVEL])
        ViewBinding.mineName.text = ConfigManager.NAME
        ViewBinding.mineSign.text = ConfigManager.SIGN
        val genders = intArrayOf(
            R.drawable.ic_gender_unknown,
            R.drawable.ic_gender_male,
            R.drawable.ic_gender_female
        )
        ViewBinding.mineGender.setImageResource(genders[ConfigManager.SEX])
        ViewBinding.mineLogout.setOnClickListener {
            XPopup.Builder(context).asConfirm(
                R.string.title_mine_logout,
                R.string.text_mine_logout_check
            ) {
                Login.startActivity(context)
            }.show()
        }
        ViewBinding.mineAbout.setOnClickListener {
            About.startActivity(context)
        }
        ViewBinding.mineSetting.setOnClickListener {
            Setting.startActivity(context)
        }
        ViewBinding.mineOtherBangumi.setOnClickListener {
            MyFollows.startActivity(context)
        }
        ViewBinding.mineDownload.setOnClickListener {
            TaskList.startActivity(context)
        }
    }
}