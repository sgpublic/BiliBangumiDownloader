package io.github.sgpublic.bilidownload.data

import android.text.Spannable
import android.text.SpannableString

data class SearchData (
        var seasonId: Long = 0L,
        var seasonTitle: Spannable = SpannableString(""),
        var seasonCover: String = "",
        var mediaScore: Double = 0.0,
        var angleTitle: String = "",
        var selectionStyle: String = "",
        var seasonContent: String = "",
        var episodeTitle: Spannable = SpannableString(""),
        var episodeCover: String = "",
        var episodeBadges: String = ""
)