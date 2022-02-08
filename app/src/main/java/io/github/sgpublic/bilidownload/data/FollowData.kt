package io.github.sgpublic.bilidownload.data

data class FollowData (
        var badge: String = "",
        var badgeColor: Int = 0,
        var badgeColorNight: Int = 0,
        var cover: String = "",
        var squareCover: String = "",
        var isFinish: Int = 0,
        var title: String = "",
        var seasonId: Long = 0L,
        var newEpCover: String = "",
        var newEpId: Long = 0L,
        var newEpIndexShow: String = "",
        var newEpIsNew: Int = 0
)