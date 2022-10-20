package io.github.sgpublic.bilidownload.app.fragment.home

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.lxj.xpopup.XPopup
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.app.activity.LoginPwd
import io.github.sgpublic.bilidownload.app.activity.MyFollows
import io.github.sgpublic.bilidownload.app.activity.Setting
import io.github.sgpublic.bilidownload.base.app.BaseFragment
import io.github.sgpublic.bilidownload.core.exsp.UserPreference
import io.github.sgpublic.bilidownload.databinding.FragmentHomeMineBinding
import io.github.sgpublic.bilidownload.app.ui.asConfirm
import io.github.sgpublic.bilidownload.app.ui.customLoad
import io.github.sgpublic.bilidownload.app.ui.withCrossFade
import io.github.sgpublic.exsp.ExPreference

class HomeMine(context: AppCompatActivity): BaseFragment<FragmentHomeMineBinding>(context) {
    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onCreateViewBinding(container: ViewGroup?): FragmentHomeMineBinding =
        FragmentHomeMineBinding.inflate(layoutInflater, container, false)

    override fun onViewSetup() {
        val user = ExPreference.get<UserPreference>()
        initViewAtTop(ViewBinding.mineToolbar)
        Glide.with(context)
            .customLoad(user.face)
            .withCrossFade()
            .into(ViewBinding.mineAvatar)
        val vipType = user.vipType
        if (vipType == 0){
            ViewBinding.mineVip.visibility = View.GONE
            ViewBinding.mineVipString.visibility = View.GONE
        } else {
            ViewBinding.mineVip.visibility = View.VISIBLE
            ViewBinding.mineVipString.visibility = View.VISIBLE
            ViewBinding.mineVipString.text = user.vipLabel
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
        ViewBinding.mineLevel.setImageResource(imageLevels[user.level])
        ViewBinding.mineName.text = user.name
        ViewBinding.mineSign.text = user.sign
        val genders = intArrayOf(
            R.drawable.ic_gender_unknown,
            R.drawable.ic_gender_male,
            R.drawable.ic_gender_female
        )
        ViewBinding.mineGender.setImageResource(genders[user.sex])
        ViewBinding.mineLogout.setOnClickListener {
            XPopup.Builder(context).asConfirm(
                R.string.title_mine_logout,
                R.string.text_mine_logout_check
            ) {
                LoginPwd.startActivity(context)
            }.show()
        }
        ViewBinding.mineAbout.setOnClickListener {
            io.github.sgpublic.bilidownload.app.activity.About.startActivity(context)
        }
        ViewBinding.mineSetting.setOnClickListener {
            Setting.startActivity(context)
        }
        ViewBinding.mineOtherBangumi.setOnClickListener {
            MyFollows.startActivity(context)
        }
        ViewBinding.mineDownload.setOnClickListener {
//            TaskList.startActivity(context)
        }
    }
}