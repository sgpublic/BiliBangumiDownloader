package io.github.sgpublic.bilidownload.core.module

import android.graphics.Color
import io.github.sgpublic.bilidownload.core.data.FollowData
import org.json.JSONObject

class FollowsModule(accessKey: String) {
    private val helper: ApiModule = ApiModule(accessKey)
    fun getFollows(mid: Long, page_index: Int, status: Int, callback: Callback) {
        val call = helper.getFollowsRequest(mid, page_index, status)
        call.enqueue(object : ApiModule.BaseOkHttpCallback(callback) {
            override fun onParseData(data: JSONObject) {
                val result = data.getJSONObject("result")
                val hasNext = result.getInt("has_next")
                val total = result.getInt("total")
                val followDataArray = ArrayList<FollowData>()
                if (total > 0) {
                    val array = result.getJSONArray("follow_list")
                    val totalPage = array.length()
                    for (followListIndex in 0 until totalPage) {
                        val index = array.getJSONObject(followListIndex)
                        val followData = FollowData()
                        followData.seasonId = index.getLong("season_id")
                        followData.title = index.getString("title")
                        followData.cover = index.getString("cover")
                        followData.isFinish = index.getInt("is_finish")
                        val badge = index.getJSONObject("badge_info")
                        followData.badge = badge.getString("text")
                        followData.badgeColor = Color.parseColor(
                            badge.getString("bg_color")
                        )
                        followData.badgeColorNight = Color.parseColor(
                            badge.getString("bg_color_night")
                        )
                        followData.squareCover = index.getString("square_cover")
                        val newEp = index.getJSONObject("new_ep")
                        followData.newEpId = newEp.getLong("id")
                        followData.newEpIsNew = newEp.getInt("is_new")
                        followData.newEpIndexShow = newEp.getString("index_show")
                        followData.newEpCover = newEp.getString("cover")
                        followDataArray.add(followData)
                    }
                }
                callback.onResult(followDataArray, hasNext == 1)
            }
        })
    }

    interface Callback : ApiModule.Callback {
        fun onResult(followData: ArrayList<FollowData>, hasNext: Boolean)
    }

    companion object {
        const val STATUS_WANT = 1
        const val STATUS_WATCHING = 2
        const val STATUS_HAS_WATCHED = 3
    }
}