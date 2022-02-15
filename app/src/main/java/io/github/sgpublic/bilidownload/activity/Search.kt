package io.github.sgpublic.bilidownload.activity

import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.BaseActivity
import io.github.sgpublic.bilidownload.base.CrashHandler
import io.github.sgpublic.bilidownload.data.SearchData
import io.github.sgpublic.bilidownload.databinding.*
import io.github.sgpublic.bilidownload.module.SearchModule
import io.github.sgpublic.bilidownload.module.SearchModule.*
import io.github.sgpublic.bilidownload.ui.addOnReadyListener
import io.github.sgpublic.bilidownload.util.MyLog
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import kotlin.math.roundToInt

class Search: BaseActivity<ActivitySearchBinding>() {
    override fun onActivityCreated(hasSavedInstanceState: Boolean) {
        getHistory()
        getHotWords()
    }

    override fun onCreateViweBinding(): ActivitySearchBinding =
        ActivitySearchBinding.inflate(layoutInflater)

    override fun onViewSetup() {
        ViewBinding.searchSuggestionCover.setOnClickListener {
            ViewBinding.searchEdit.clearFocus()
        }
        ViewBinding.searchBack.setOnClickListener {
            onBackPressed()
        }
        ViewBinding.searchEdit.addTextChangedListener(afterTextChanged = {
            if (ViewBinding.searchEdit.hasFocus()){
                getSuggestions()
            }
        })
        ViewBinding.searchEdit.setOnEditorActionListener listener@{ _, id, _ ->
            MyLog.d("code: $id")
            if (id == EditorInfo.IME_ACTION_SEARCH) {
                ViewBinding.searchEdit.clearFocus()
                onSearch()
                return@listener true
            }
            return@listener false
        }
    }

    private fun getHotWords() {
        val helper = SearchModule(this@Search)
        helper.getHotWord(object : HotWordCallback {
            override fun onResult(hotWords: List<String>) {
                val paramsLinear = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                for (array_index in hotWords.indices) {
                    val historyIndex = hotWords[array_index]
                    val itemSearchWord = ItemSearchWordBinding.inflate(layoutInflater, ViewBinding.searchHotWordList, false)
                    itemSearchWord.itemWordTitle.text = historyIndex
                    itemSearchWord.itemWordTitle.setOnClickListener {
                        onSearch(historyIndex)
                    }
                    runOnUiThread {
                        ViewBinding.searchHotWordList.addView(itemSearchWord.root, paramsLinear)
                    }
                }
                setAnimateState(true, 500, ViewBinding.searchHotWord)
            }
        })
    }

    private fun getSuggestions() {
        val helper = SearchModule(this@Search)
        helper.suggest(ViewBinding.searchEdit.text.toString(), object : SuggestCallback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                setAnimateState(false, 150, ViewBinding.searchSuggestionBase, null)
            }

            override fun onResult(suggestions: List<Spannable>) {
                val rowCount = suggestions.size
                setAnimateState(false, 150, ViewBinding.searchSuggestionBase) {
                    if (rowCount == 0) {
                        return@setAnimateState
                    }
                    setAnimateState(true, 150, ViewBinding.searchSuggestionBase) {
                        ViewBinding.searchSuggestionResult.removeAllViews()
                        ViewBinding.searchSuggestionResult.columnCount = 1
                        ViewBinding.searchSuggestionResult.rowCount = rowCount
                        val viewHeight = Application.dip2px(50f)
                        val viewWidth =
                            resources.displayMetrics.widthPixels - Application.dip2px(40f)
                        for (suggestion_index in 0 until rowCount) {
                            val spannableIndex = suggestions[suggestion_index]
                            val view = ItemSearchSuggestionBinding.inflate(layoutInflater, ViewBinding.searchSuggestionResult, false)
                            view.itemSuggestionTitle.text = spannableIndex
                            view.itemSuggestionTitle.setOnClickListener {
                                ViewBinding.searchEdit.clearFocus()
                                onSearch(spannableIndex.toString())
                            }
                            val params =
                                GridLayout.LayoutParams()
                            params.rowSpec = GridLayout.spec(suggestion_index)
                            params.columnSpec = GridLayout.spec(0)
                            params.height = viewHeight
                            params.width = viewWidth
                            view.root.layoutParams = params
                            ViewBinding.searchSuggestionResult.addView(view.root)
                        }
                    }
                }
            }
        })
    }

    private fun onSearch(keyword: String) {
        ViewBinding.searchEdit.setText(keyword)
        onSearch()
    }

    private fun onSearch() {
        ViewBinding.searchEdit.clearFocus()
        val viewVisible: ScrollView = if (ViewBinding.searchMain.visibility == View.VISIBLE) {
            ViewBinding.searchMain
        } else {
            ViewBinding.searchResult
        }
        setAnimateState(false, 300, viewVisible) {
            ViewBinding.searchLoadState.startLoad()
            setAnimateState(true, 300, ViewBinding.searchLoadState)
            val keyword: String = ViewBinding.searchEdit.text.toString()
            onAddHistory(keyword)
            val helper = SearchModule(this@Search)
            helper.search(keyword, object : SearchCallback {
                override fun onFailure(code: Int, message: String?, e: Throwable?) {
                    Application.onToast(this@Search, R.string.error_bangumi_load, message, code)
                    runOnUiThread {
                        ViewBinding.searchLoadState.stopLoad(true)
                    }
                }

                override fun onResult(searchData: List<SearchData>) {
                    setAnimateState(false, 300, ViewBinding.searchLoadState) animate@{
                        if (searchData.isEmpty()) {
                            setAnimateState(true, 300, ViewBinding.searchLoadState) {
                                ViewBinding.searchLoadState.stopLoad(true)
                            }
                            return@animate
                        }
                        setAnimateState(true, 300, ViewBinding.searchResult) {
                            ViewBinding.searchLoadState.stopLoad()
                            ViewBinding.searchResultList.removeAllViews()
                            for (data_index in searchData.indices) {
                                val data = searchData[data_index]
                                val searchItem: View? = when (data.selectionStyle) {
                                    "grid" -> {
                                        getGridSearchView(data)
                                    }
                                    "horizontal" -> {
                                        getHorizontalSearchView(data)
                                    }
                                    else -> {
                                        null
                                    }
                                }
                                if (searchItem != null) {
                                    val params =
                                        GridLayout.LayoutParams()
                                    params.width =
                                        resources.displayMetrics.widthPixels
                                    params.columnSpec = GridLayout.spec(0)
                                    params.rowSpec = GridLayout.spec(data_index)
                                    ViewBinding.searchResultList.addView(searchItem, params)
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    private fun getGridSearchView(data: SearchData): View {
        val searchItem = ItemSearchSeasonBinding.inflate(
            layoutInflater,
            ViewBinding.searchResultList,
            false
        )
        searchItem.itemSearchSeasonTitle.text = data.seasonTitle
        searchItem.itemSearchSeasonContent.text = data.seasonContent
        searchItem.itemSearchRatingString.text = data.mediaScore.toString()
        if (data.seasonBadge == "") {
            searchItem.itemSeasonBadges.visibility = View.GONE
        } else {
            searchItem.itemSeasonBadges.visibility = View.VISIBLE
            searchItem.itemSeasonBadges.text = data.seasonBadge
        }
        if (data.mediaScore == 0.0) {
            searchItem.itemSearchRatingNull.visibility = View.VISIBLE
            searchItem.itemSearchRatingString.visibility = View.INVISIBLE
        } else {
            searchItem.itemSearchRatingNull.visibility = View.INVISIBLE
            searchItem.itemSearchRatingString.visibility = View.VISIBLE
        }
        searchItem.itemSearchRatingStart.progress = data.mediaScore.roundToInt()
        searchItem.root.setOnClickListener {
            goToSeason(data)
        }
        val requestOptions = RequestOptions()
            .placeholder(R.drawable.pic_doing_v)
            .error(R.drawable.pic_load_failed)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        Glide.with(this@Search)
            .load(data.seasonCover)
            .apply(requestOptions)
            .addOnReadyListener {
                setAnimateState(false, 400, searchItem.itemSearchSeasonPlaceholder) {
                    setAnimateState(true, 400, searchItem.itemSearchSeasonCover)
                }
            }
            .into(searchItem.itemSearchSeasonCover)
        return searchItem.root
    }

    private fun getHorizontalSearchView(data: SearchData): View {
        val searchItem = ItemSearchEpisodeBinding.inflate(
            layoutInflater,
            ViewBinding.searchResultList,
            false
        )
        searchItem.itemSearchEpisodeTitle.text = data.episodeTitle
        searchItem.itemSearchEpisodeFrom.text = String.format(
            getString(R.string.text_search_from),
            data.seasonTitle.toString()
        )
        if ("" == data.episodeBadge) {
            searchItem.itemEpisodeBadges.visibility = View.GONE
        } else {
            searchItem.itemEpisodeBadges.visibility =
                View.VISIBLE
            searchItem.itemEpisodeBadges.text = data.episodeBadge
        }
        searchItem.root.setOnClickListener {
            goToSeason(data)
        }
        val requestOptions = RequestOptions()
            .placeholder(R.drawable.pic_doing_h)
            .error(R.drawable.pic_load_failed)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        Glide.with(this@Search)
            .load(data.episodeCover)
            .apply(requestOptions)
            .into(searchItem.itemSearchEpisodeCover)
        return searchItem.root
    }

    private fun goToSeason(data: SearchData) {
        SeasonPlayer.startActivity(this@Search, data.seasonId)
    }

    private fun getHistory() {
        try {
            val fileInputStream = applicationContext.openFileInput("history.json")
            val bufferedReader = BufferedReader(InputStreamReader(fileInputStream))
            var line: String?
            val stringBuilder = StringBuilder()
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            val historyContent = stringBuilder.toString()
            if (historyContent != "") {
                val array = JSONArray(historyContent)
                val paramsLinear = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                for (array_index in 0 until array.length()) {
                    val historyIndex = array.getString(array_index)
                    val itemSearchWord = ItemSearchWordBinding.inflate(
                        layoutInflater,
                        ViewBinding.searchHistoryList,
                        false
                    )
                    itemSearchWord.itemWordTitle.text = historyIndex
                    itemSearchWord.itemWordTitle.setOnClickListener {
                        onSearch(historyIndex)
                    }
                    ViewBinding.searchHistoryList.addView(itemSearchWord.root, paramsLinear)
                }
                setAnimateState(true, 500, ViewBinding.searchHistory)
            }
        } catch (e: IOException) {
            if (e !is FileNotFoundException) {
                CrashHandler.saveExplosion(e, -705)
            }
        } catch (e: JSONException) {
            CrashHandler.saveExplosion(e, -703)
        }
    }

    private fun onAddHistory(keyword: String) {
        try {
            val arraySave = JSONArray()
            arraySave.put(keyword)
            try {
                val fileInputStream = applicationContext.openFileInput("history.json")
                val bufferedReader = BufferedReader(InputStreamReader(fileInputStream))
                var line: String?
                val stringBuilder = java.lang.StringBuilder()
                while (bufferedReader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
                val historyContent = stringBuilder.toString()
                if (historyContent != "") {
                    val array = JSONArray(historyContent)
                    var arrayIndex = 0
                    while (arrayIndex < array.length() && arrayIndex < 10) {
                        val historyIndex = array.getString(arrayIndex)
                        if (historyIndex != keyword) {
                            arraySave.put(historyIndex)
                        }
                        arrayIndex++
                    }
                }
            } catch (ignore: FileNotFoundException) {
            }
            val fileOutputStream = applicationContext.openFileOutput("history.json", MODE_PRIVATE)
            fileOutputStream.write(arraySave.toString().toByteArray())
            fileOutputStream.close()
        } catch (e: IOException) {
            CrashHandler.saveExplosion(e, -715)
        } catch (e: JSONException) {
            CrashHandler.saveExplosion(e, -713)
        }
    }

    override fun onBackPressed() {
        if (ViewBinding.searchEdit.hasFocus()) {
            ViewBinding.searchEdit.clearFocus()
        } else {
            finish()
        }
    }

    companion object {
        fun startActivity(context: Context){
            val intent = Intent(context, Search::class.java)
            context.startActivity(intent)
        }
    }
}