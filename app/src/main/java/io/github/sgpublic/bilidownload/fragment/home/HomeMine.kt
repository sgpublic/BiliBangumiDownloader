package io.github.sgpublic.bilidownload.fragment.home

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.kongzue.dialog.v3.MessageDialog
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.activity.About
import io.github.sgpublic.bilidownload.activity.Login
import io.github.sgpublic.bilidownload.activity.MyFollows
import io.github.sgpublic.bilidownload.activity.Setting
import io.github.sgpublic.bilidownload.base.BaseFragment
import io.github.sgpublic.bilidownload.databinding.FragmentHomeMineBinding
import io.github.sgpublic.bilidownload.manager.ConfigManager

class HomeMine(private val context: AppCompatActivity): BaseFragment<FragmentHomeMineBinding>(context) {
    override fun onFragmentCreated(savedInstanceState: Bundle?) {

    }

    override fun onViewSetup() {
        initViewAtTop(binding.mineToolbar)
        Glide.with(context)
            .load(ConfigManager.FACE)
            .into(binding.mineAvatar)
        val vipType = ConfigManager.VIP_TYPE
        if (vipType == 0){
            binding.mineVip.visibility = View.GONE
            binding.mineVipString.visibility = View.GONE
        } else {
            binding.mineVip.visibility = View.VISIBLE
            binding.mineVipString.visibility = View.VISIBLE
            binding.mineVipString.text = ConfigManager.VIP_LABEL
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
        binding.mineLevel.setImageResource(imageLevels[ConfigManager.LEVEL])
        binding.mineName.text = ConfigManager.NAME
        binding.mineSign.text = ConfigManager.SIGN
        val genders = intArrayOf(
            R.drawable.ic_gender_unknown,
            R.drawable.ic_gender_male,
            R.drawable.ic_gender_female
        )
        binding.mineGender.setImageResource(genders[ConfigManager.SEX])
        binding.mineLogout.setOnClickListener {
            MessageDialog.build(context)
                .setTitle(R.string.title_mine_logout)
                .setMessage(R.string.text_mine_logout_check)
                .setOkButton(R.string.text_ok) { _, _ ->
                    Login.startActivity(context)
                    false
                }
                .setCancelButton(R.string.text_cancel)
                .show()
        }
        binding.mineAbout.setOnClickListener {
            About.startActivity(context)
        }
        binding.mineSetting.setOnClickListener {
            Setting.startActivity(context)
        }
        binding.mineOtherBangumi.setOnClickListener {
            MyFollows.startActivity(context)
        }
    }
}