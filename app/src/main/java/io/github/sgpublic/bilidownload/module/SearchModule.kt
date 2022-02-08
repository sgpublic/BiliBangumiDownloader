package io.github.sgpublic.bilidownload.module

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.data.SearchData
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
                    callback.onFailure(-801, null, e)
                } else {
                    callback.onFailure(-802, null, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = response.body.toString()
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
                        callback.onFailure(-814, json.getString("message"), null)
                    }
                } catch (e: JSONException) {
                    callback.onFailure(-813, null, e)
                }
            }
        })
    }

    fun suggest(keyword: String, callback: SuggestCallback) {
        val call = helper.getSearchSuggestRequest(keyword)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback.onFailure(-811, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-812, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = response.body.toString()
                try {
                    var json = JSONObject(result)
                    if (json.getInt("code") == 0) {
                        val suggestions = ArrayList<Spannable>()
                        try {
                            val array = json.getJSONObject("result").getJSONArray("tag")
                            var arrayIndex = 0
                            while (arrayIndex < 7 && arrayIndex < array.length()) {
                                json = array.getJSONObject(arrayIndex)
                                val valueString = json.getString("value")
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
                            callback.onFailure(-815, null, null)
                        }
                    } else {
                        callback.onFailure(-814, null, null)
                    }
                } catch (e: JSONException) {
                    callback.onFailure(-813, null, e)
                }
            }
        })
    }

    fun search(keyword: String, callback: SearchCallback) {
        val call = helper.getSearchResultRequest(keyword)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback.onFailure(-821, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-822, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = response.body.toString()
                try {
                    var json = JSONObject(result)
                    if (json.getInt("code") == 0) {
                        val searchDataList = ArrayList<SearchData>()
                        json = json.getJSONObject("data")
                        if (!json.isNull("result")) {
                            val array = json.getJSONArray("result")
                            for (array_index in 0 until array.length()) {
                                json = array.getJSONObject(array_index)
                                val searchData = SearchData()
                                searchData.angleTitle = json.getString("angle_title")
                                searchData.seasonCover = "http:" + json.getString("cover")
                                if (json.isNull("media_score")) {
                                    searchData.mediaScore = 0.0
                                } else {
                                    searchData.mediaScore = json.getJSONObject("media_score")
                                        .getDouble("score")
                                }
                                searchData.seasonId = json.getLong("season_id")
                                //searchData.season_title = object.getString("season_title");
                                val seasonTitleString = json.getString("title")
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
                                if (json.getLong("pubtime") * 1000 > System.currentTimeMillis()) {
                                    searchData.selectionStyle = "grid"
                                } else {
                                    searchData.selectionStyle =
                                        json.getString("selection_style")
                                }
                                val date = Date(json.getLong("pubtime") * 1000)
                                val format = SimpleDateFormat("yyyy", Locale.CHINA)
                                searchData.seasonContent = format.format(date) + "｜"
                                searchData.seasonContent = json.getString("season_type_name") + "｜" +
                                        json.getString("areas") + "\n" +
                                        json.getString("styles")
                                var epsArray = json.getJSONArray("eps")
                                if (epsArray.length() > 0) {
                                    json = epsArray.getJSONObject(0)
                                    searchData.episodeCover = json.getString("cover")
                                    val episodeTitleString = json.getString("long_title")
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
                                    epsArray = json.getJSONArray("badges")
                                    if (epsArray.length() > 0) {
                                        json = epsArray.getJSONObject(0)
                                        searchData.episodeBadges = json.getString("text")
                                    } else {
                                        searchData.episodeBadges = ""
                                    }
                                }
                                searchDataList.add(searchData)
                            }
                        }
                        callback.onResult(searchDataList)
                    } else {
                        callback.onFailure(-824, json.getString("message"), null)
                    }
                } catch (e: JSONException) {
                    callback.onFailure(-823, null, e)
                }
            }
        })
    }

    interface SearchCallback {
        fun onFailure(code: Int, message: String?, e: Throwable?)
        fun onResult(searchData: ArrayList<SearchData>)
    }

    interface SuggestCallback {
        fun onFailure(code: Int, message: String?, e: Throwable?)
        fun onResult(suggestions: ArrayList<Spannable>)
    }

    interface HotWordCallback {
        fun onFailure(code: Int, message: String?, e: Throwable?)
        fun onResult(hotWords: ArrayList<String>)
    }
}