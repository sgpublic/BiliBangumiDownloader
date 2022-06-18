package io.github.sgpublic.bilidownload.core.data

import android.text.Spannable
import android.text.SpannableString

data class SearchData(
    var seasonId: Long = 0L,
    var seasonTitle: Spannable = SpannableString(""),
    var seasonCover: String = "",
    var mediaScore: Double = 0.0,
    var seasonBadge: String = "",
    var seasonBadgeColor: Int = 0,
    var seasonBadgeColorNight: Int = 0,
    var selectionStyle: String = "",
    var seasonContent: String = "",
    var episodeTitle: Spannable = SpannableString(""),
    var episodeCover: String = "",
    var episodeBadge: String = "",
    var episodeBadgeColor: Int = 0,
    var episodeBadgeColorNight: Int = 0
)