package io.github.sgpublic.bilidownload.app.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import com.bumptech.glide.Glide
import com.lxj.xpopup.core.BottomPopupView
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp
import io.github.sgpublic.bilidownload.core.util.*
import io.github.sgpublic.bilidownload.databinding.DialogSeasonInfoBinding
import kotlin.math.roundToInt

/**
 *
 * @author Madray Haven
 * @date 2022/10/27 16:01
 */

@SuppressLint("ViewConstructor")
class SeasonInfoDialog(context: Context, private val data: SeasonInfoResp.SeasonInfoData)
    : BottomPopupView(context) {
    override fun onCreate() {
        val binding: DialogSeasonInfoBinding = DialogSeasonInfoBinding.bind(popupImplView)
        binding.seasonTitle.text = data.seasonTitle
        Glide.with(context)
            .customLoad(data.refineCover)
            .withVerticalPlaceholder()
            .withCrossFade()
            .into(binding.seasonCover)
        if (data.rating.score == 0f) {
            binding.seasonRatingString.visibility = View.INVISIBLE
            binding.seasonRatingNull.visibility = View.VISIBLE
            binding.seasonRatingStar.progress = 0
        } else {
            binding.seasonRatingNull.visibility = View.INVISIBLE
            binding.seasonRatingString.visibility = View.VISIBLE
            binding.seasonRatingString.text = String.format("%.1f", data.rating.score)
            binding.seasonRatingStar.progress = data.rating.score.roundToInt()
        }
        binding.seasonStaffTitle.text = data.staff.title
        binding.seasonStaff.setOnClickListener {
            val lines = data.staff.info.countLine()
            binding.seasonStaff.maxLines = (lines == binding.seasonStaff.maxLines).take(3, lines)
        }
        if (data.actor != null) {
            binding.seasonActorsTitle.text = data.actor!!.title
            binding.seasonActors.text = data.actor!!.info
            binding.seasonActors.setOnClickListener {
                val lines = data.actor!!.info.countLine()
                binding.seasonActors.maxLines = (lines == binding.seasonActors.maxLines).take(3, lines)
            }
        } else if (data.celebrity != null) {

        }
        binding.seasonContent.text = data.typeDesc
        if (data.originName != "") {
            binding.seasonOriginName.text = data.originName
        } else {
            binding.seasonOriginName.visibility = View.GONE
            binding.seasonOriginNameTitle.visibility = View.GONE
        }
        if (data.styles.isNotEmpty()) {
            // TODO 风格列表
//            binding.seasonStyles.text = data.styles
        } else {
            binding.seasonStyles.visibility = View.GONE
            binding.seasonStylesTitle.visibility = View.GONE
        }
        if (data.evaluate != "") {
            binding.seasonEvaluate.text = data.evaluate
        } else {
            binding.seasonEvaluate.visibility = View.GONE
            binding.seasonEvaluateTitle.visibility = View.GONE
        }
    }

    override fun getImplLayoutId(): Int = R.layout.dialog_season_info
}