package io.github.sgpublic.bilidownload.module

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.data.SearchData
import io.github.sgpublic.bilidownload.util.MyLog
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

class SearchModule(private val context: Context) {
    private val helper: BaseAPI = BaseAPI()

    fun getHotWord(callback: HotWordCallback) {
        val call = helper.getHotWordRequest()
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback.postFailure(-801, null, e)
                } else {
                    callback.postFailure(-802, null, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                try {
                    var json = JSONObject(result)
                    if (json.getInt("code") == 0) {
                        val hotWords = ArrayList<String>()
                        val array = json.getJSONArray("list")
                        for (array_index in 0 until array.length()) {
                            json = array.getJSONObject(array_index)
                            hotWords.add(json.getString("keyword"))
                        }
                        callback.onResult(hotWords)
                    } else {
                        callback.postFailure(-814, json.getString("message"), null)
                    }
                } catch (e: JSONException) {
                    callback.postFailure(-813, null, e)
                }
            }
        })
    }

    fun suggest(keyword: String, callback: SuggestCallback) {
        val call = helper.getSearchSuggestRequest(keyword)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback.postFailure(-811, context.getString(R.string.error_network), e)
                } else {
                    callback.postFailure(-812, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                try {
                    val json = JSONObject(result)
                    if (json.getInt("code") != 0) {
                        callback.postFailure(-814, json.getString("message"), null)
                    }
                    val suggestions = ArrayList<Spannable>()
                    try {
                        val array = json.getJSONObject("result").getJSONArray("tag")
                        var arrayIndex = 0
                        while (arrayIndex < 7 && arrayIndex < array.length()) {
                            val data = array.getJSONObject(arrayIndex)
                            val valueString = data.getString("value")
                            val valueSpannable: Spannable = SpannableString(valueString)
                            for (value_index in keyword.indices) {
                                val keywordIndex =
                                    keyword.substring(value_index).substring(0, 1)
                                val valueStringSub = valueString.indexOf(keywordIndex)
                                if (valueStringSub >= 0) {
                                    valueSpannable.setSpan(
                                        ForegroundColorSpan(context.getColor(R.color.colorPrimary)),
                                        valueStringSub, valueStringSub + 1,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                }
                            }
                            suggestions.add(valueSpannable)
                            arrayIndex++
                        }
                        callback.onResult(suggestions)
                    } catch (e: JSONException) {
                        callback.postFailure(-815, null, e)
                    }
                } catch (e: JSONException) {
                    callback.postFailure(-813, null, e)
                }
            }
        })
    }

    fun search(keyword: String, callback: SearchCallback) {
        val call = helper.getSearchResultRequest(keyword)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback.postFailure(-821, context.getString(R.string.error_network), e)
                } else {
                    callback.postFailure(-822, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                try {
                    val json = JSONObject(result)
                    if (json.getInt("code") != 0) {
                        callback.postFailure(-824, json.getString("message"), null)
                        return
                    }
                    val data = json.getJSONObject("data")
                    if (data.isNull("result")) {
                        MyLog.d("empty result")
                        callback.onResult(listOf())
                        return
                    }
                    val searchDataList = ArrayList<SearchData>()
                    val array = data.getJSONArray("result")
                    for (i in 0 until array.length()) {
                        val index = array.getJSONObject(i)
                        val searchData = SearchData()
                        if (!index.isNull("badges")) {
                            val seasonBadge = index.getJSONArray("badges").getJSONObject(0)
                            searchData.seasonBadge = seasonBadge.getString("text")
                            searchData.seasonBadgeColor = Color.parseColor(
                                seasonBadge.getString("bg_color")
                            )
                            searchData.seasonBadgeColorNight = Color.parseColor(
                                seasonBadge.getString("bg_color_night")
                            )
                        }
                        searchData.seasonCover = index.getString("cover")
                        if (index.isNull("media_score")) {
                            searchData.mediaScore = 0.0
                        } else {
                            searchData.mediaScore = index.getJSONObject("media_score")
                                .getDouble("score")
                        }
                        searchData.seasonId = index.getLong("season_id")
                        //searchData.season_title = object.getString("season_title");
                        val seasonTitleString = index.getString("title")
                        val seasonTitleSpannable: Spannable = SpannableString(
                            seasonTitleString
                                .replace("<em class=\"keyword\">", "")
                                .replace("</em>", "")
                        )
                        val seasonTitleSubStart =
                            seasonTitleString.indexOf("<em class=\"keyword\">")
                        val seasonTitleSubEnd = seasonTitleString
                            .replace("<em class=\"keyword\">", "")
                            .indexOf("</em>")
                        if (seasonTitleSubStart >= 0 && seasonTitleSubEnd >= 0) {
                            seasonTitleSpannable.setSpan(
                                ForegroundColorSpan(context.getColor(R.color.colorPrimary)),
                                seasonTitleSubStart, seasonTitleSubEnd,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                        searchData.seasonTitle = seasonTitleSpannable
                        if (index.getLong("pubtime") * 1000 > System.currentTimeMillis()) {
                            searchData.selectionStyle = "grid"
                        } else {
                            searchData.selectionStyle =
                                index.getString("selection_style")
                        }
                        val date = Date(index.getLong("pubtime") * 1000)
                        val format = SimpleDateFormat("yyyy", Locale.CHINA)
                        searchData.seasonContent = format.format(date) + "｜"
                        searchData.seasonContent = index.getString("season_type_name") + "｜" +
                                index.getString("areas") + "\n" +
                                index.getString("styles")
                        val epsArray = index.getJSONArray("eps")
                        if (epsArray.length() > 0) {
                            val episode = epsArray.getJSONObject(0)
                            searchData.episodeCover = episode.getString("cover")
                            val episodeTitleString = episode.getString("long_title")
                            val episodeTitleSpannable: Spannable = SpannableString(
                                episodeTitleString
                                    .replace("<em class=\"keyword\">", "")
                                    .replace("</em>", "")
                            )
                            val episodeTitleSubStart =
                                episodeTitleString.indexOf("<em class=\"keyword\">")
                            val episodeTitleSubEnd = episodeTitleString
                                .replace("<em class=\"keyword\">", "")
                                .indexOf("</em>")
                            if (episodeTitleSubStart >= 0 && episodeTitleSubEnd >= 0) {
                                episodeTitleSpannable.setSpan(
                                    ForegroundColorSpan(context.getColor(R.color.colorPrimary)),
                                    episodeTitleSubStart, episodeTitleSubEnd,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            }
                            searchData.episodeTitle = episodeTitleSpannable
                            if (!episode.isNull("badges")) {
                                val episodeBadge = episode.getJSONArray("badges").getJSONObject(0)
                                searchData.episodeBadge = episodeBadge.getString("text")
                                searchData.episodeBadgeColor = Color.parseColor(
                                    episodeBadge.getString("bg_color")
                                )
                                searchData.episodeBadgeColorNight = Color.parseColor(
                                    episodeBadge.getString("bg_color_night")
                                )
                            }
                        }
                        searchDataList.add(searchData)
                    }
                    callback.onResult(searchDataList)
                } catch (e: JSONException) {
                    callback.postFailure(-823, null, e)
                }
            }
        })
    }

    interface SearchCallback : BaseAPI.BaseInterface {
        fun onResult(searchData: List<SearchData>)
    }

    interface SuggestCallback : BaseAPI.BaseInterface {
        fun onResult(suggestions: List<Spannable>)
    }

    interface HotWordCallback : BaseAPI.BaseInterface {
        override fun onFailure(code: Int, message: String?, e: Throwable?) {}
        fun onResult(hotWords: List<String>)
    }
}