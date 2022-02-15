package io.github.sgpublic.bilidownload.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import com.bumptech.glide.Glide
import com.lxj.xpopup.core.BottomPopupView
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.data.SeasonData
import io.github.sgpublic.bilidownload.databinding.DialogSeasonInfoBinding
import kotlin.math.roundToInt

@SuppressLint("ViewConstructor")
class SeasonInfoDialog(context: Context, private val data: SeasonData)
    : BottomPopupView(context) {
    override fun onCreate() {
        val binding: DialogSeasonInfoBinding = DialogSeasonInfoBinding.bind(popupImplView)
        binding.seasonTitle.text = data.info.title
        Glide.with(context).load(data.info.cover)
            .into(binding.seasonCover)
        if (data.rating == 0.0) {
            binding.seasonRatingString.visibility = View.INVISIBLE
            binding.seasonRatingNull.visibility = View.VISIBLE
            binding.seasonRatingStar.progress = 0
        } else {
            binding.seasonRatingNull.visibility = View.INVISIBLE
            binding.seasonRatingString.visibility = View.VISIBLE
            binding.seasonRatingString.text = data.rating.toString()
            binding.seasonRatingStar.progress = data.rating.roundToInt()
        }
        binding.seasonStuff.setOnClickListener {
            binding.seasonStuff.maxLines =
                if (data.staffLines == binding.seasonStuff.maxLines) 3 else data.staffLines
        }
        binding.seasonActors.setOnClickListener {
            binding.seasonActors.maxLines =
                if (data.actorsLines == binding.seasonActors.maxLines) 3 else data.actorsLines
        }
        binding.seasonContent.text = data.description
        if (data.alias != "") {
            binding.seasonAlias.text = data.alias
        } else {
            binding.seasonAlias.visibility = View.GONE
            binding.seasonAliasTitle.visibility = View.GONE
        }
        if (data.styles != "") {
            binding.seasonStyles.text = data.styles
        } else {
            binding.seasonStyles.visibility = View.GONE
            binding.seasonStylesTitle.visibility = View.GONE
        }
        if (data.actors != "") {
            binding.seasonActors.text = data.actors
        } else {
            binding.seasonActors.visibility = View.GONE
            binding.seasonActorsTitle.visibility = View.GONE
        }
        if (data.staff != "") {
            binding.seasonStuff.text = data.staff
        } else {
            binding.seasonStuff.visibility = View.GONE
            binding.seasonStuffTitle.visibility = View.GONE
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