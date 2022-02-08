package io.github.sgpublic.bilidownload.activity

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Spannable
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.BaseActivity
import io.github.sgpublic.bilidownload.base.CrashHandler
import io.github.sgpublic.bilidownload.data.SearchData
import io.github.sgpublic.bilidownload.databinding.*
import io.github.sgpublic.bilidownload.module.SearchModule
import io.github.sgpublic.bilidownload.module.SearchModule.*
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import kotlin.math.roundToInt

class Search: BaseActivity<ActivitySearchBinding>() {
    private var isSuggesting = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        getHistory()
        getHotWords()
    }

    private val timerSuggest = Timer()
    private val timerTask = object : TimerTask(){
        override fun run() {
            getSuggestions()
        }
    }
    override fun onViewSetup() {
        binding.searchSuggestionCover.setOnClickListener {
            binding.searchEdit.clearFocus()
        }
        binding.searchEdit.addTextChangedListener {
            if (!isSuggesting){
                return@addTextChangedListener
            }
            timerSuggest.cancel()
            timerSuggest.schedule(timerTask, 300)
        }
    }

    private fun getHotWords() {
        val helper = SearchModule(this@Search)
        helper.getHotWord(object : HotWordCallback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                if (e is JSONException) {
                    CrashHandler.saveExplosion(e, code)
                }
            }

            override fun onResult(hotWords: ArrayList<String>) {
                val paramsLinear = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                for (array_index in hotWords.indices) {
                    val historyIndex = hotWords[array_index]
                    val itemSearchWord = ItemSearchWordBinding.inflate(layoutInflater, binding.searchHotWordList, false)
                    itemSearchWord.itemWordTitle.text = historyIndex
                    itemSearchWord.itemWordTitle.setOnClickListener {
                        onSearch(historyIndex)
                    }
                    runOnUiThread {
                        binding.searchHotWordList.addView(itemSearchWord.root, paramsLinear)
                    }
                }
                setAnimateState(true, 500, binding.searchHotWord)
            }
        })
    }

    private fun getSuggestions() {
        val helper = SearchModule(this@Search)
        helper.suggest(binding.searchEdit.text.toString(), object : SuggestCallback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                setAnimateState(false, 150, binding.searchSuggestionBase, null)
                if (e is JSONException) {
                    CrashHandler.saveExplosion(e, code)
                }
            }

            override fun onResult(suggestions: ArrayList<Spannable>) {
                val rowCount = suggestions.size
                setAnimateState(false, 150, binding.searchSuggestionBase) {
                    if (rowCount != 0) {
                        setAnimateState(true, 150, binding.searchSuggestionBase) {
                            binding.searchSuggestionResult.removeAllViews()
                            binding.searchSuggestionResult.columnCount = 1
                            binding.searchSuggestionResult.rowCount = rowCount
                            val viewHeight = Application.dip2px(50f)
                            val viewWidth =
                                resources.displayMetrics.widthPixels - Application.dip2px(40f)
                            for (suggestion_index in 0 until rowCount) {
                                val spannableIndex = suggestions[suggestion_index]
                                val view = ItemSearchSuggestionBinding.inflate(layoutInflater, binding.searchSuggestionResult, false)
                                view.itemSuggestionTitle.text = spannableIndex
                                view.itemSuggestionTitle.setOnClickListener {
                                    binding.searchEdit.clearFocus()
                                    onSearch(spannableIndex.toString())
                                }
                                val params =
                                    GridLayout.LayoutParams()
                                params.rowSpec = GridLayout.spec(suggestion_index)
                                params.columnSpec = GridLayout.spec(0)
                                params.height = viewHeight
                                params.width = viewWidth
                                view.root.layoutParams = params
                                binding.searchSuggestionResult.addView(view.root)
                            }
                        }
                    }
                }
            }
        })
    }

    private fun onSearch(keyword: String) {
        binding.searchEdit.setText(keyword)
        onSearch()
    }

    private fun onSearch() {
        val viewVisible: ScrollView = if (binding.searchMain.visibility == View.VISIBLE) {
            binding.searchMain
        } else {
            binding.searchResult
        }
        setAnimateState(false, 300, viewVisible) {
            startOnLoadingState(binding.searchLoadState)
            setAnimateState(true, 300, binding.searchLoadState) {
                val keyword: String = binding.searchEdit.text.toString()
                onAddHistory(keyword)
                val helper = SearchModule(this@Search)
                helper.search(keyword, object : SearchCallback {
                    override fun onFailure(code: Int, message: String?, e: Throwable?) {
                        onToast(R.string.error_bangumi_load, message, code)
                        runOnUiThread {
                            stopOnLoadingState()
                            binding.searchLoadState.setImageResource(R.drawable.pic_load_failed)
                        }
                        CrashHandler.saveExplosion(e, code)
                    }

                    override fun onResult(searchData: ArrayList<SearchData>) {
                        setAnimateState(false, 300, binding.searchLoadState) {
                            stopOnLoadingState()
                            if (searchData.size > 0) {
                                setAnimateState(true, 300, binding.searchResult) {
                                    binding.searchResultList.removeAllViews()
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
                                            binding.searchResultList.addView(searchItem, params)
                                        }
                                    }
                                }
                            } else {
                                setAnimateState(true, 300, binding.searchLoadState) {
                                    binding.searchLoadState.setImageResource(R.drawable.pic_null)
                                }
                            }
                        }
                    }
                })
            }
        }
    }

    private fun getGridSearchView(data: SearchData): View {
        val searchItem = ItemSearchSeasonBinding.inflate(
            layoutInflater,
            binding.searchResultList,
            false
        )
        searchItem.itemSearchSeasonTitle.text = data.seasonTitle
        searchItem.itemSearchSeasonContent.text = data.seasonContent
        searchItem.itemSearchRatingString.text = data.mediaScore.toString()
        if (data.angleTitle == "") {
            searchItem.itemSeasonBadges.visibility = View.GONE
        } else {
            searchItem.itemSeasonBadges.visibility = View.VISIBLE
            searchItem.itemSeasonBadges.text = data.angleTitle
        }
        if (data.mediaScore == 0.0) {
            searchItem.itemSearchRatingNull.visibility = View.VISIBLE
            searchItem.itemSearchRatingString.visibility = View.INVISIBLE
        } else {
            searchItem.itemSearchRatingNull.visibility = View.INVISIBLE
            searchItem.itemSearchRatingString.visibility = View.VISIBLE
        }
        searchItem.itemSearchRatingStart.progress = data.mediaScore.roundToInt()
        searchItem.itemSearchBaseGo.setOnClickListener {
            goToSeason(data)
        }
        searchItem.itemSearchGo.setOnClickListener {
            goToSeason(data)
        }
        val requestOptions = RequestOptions()
            .placeholder(R.drawable.pic_doing_v)
            .error(R.drawable.pic_load_failed)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        Glide.with(this@Search)
            .load(data.seasonCover)
            .apply(requestOptions)
            .addListener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ) = false

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any,
                    target: Target<Drawable?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    setAnimateState(false, 400, searchItem.itemSearchSeasonPlaceholder) {
                        setAnimateState(true, 400, searchItem.itemSearchSeasonCover)
                    }
                    return false
                }
            })
            .into(searchItem.itemSearchSeasonCover)
        return searchItem.root
    }

    private fun getHorizontalSearchView(data: SearchData): View {
        val searchItem = ItemSearchEpisodeBinding.inflate(
            layoutInflater,
            binding.searchResultList,
            false
        )
        searchItem.itemSearchEpisodeTitle.text = data.episodeTitle
        searchItem.itemSearchEpisodeFrom.text = String.format(
            getString(R.string.text_search_from),
            data.seasonTitle.toString()
        )
        if ("" == data.episodeBadges) {
            searchItem.itemEpisodeBadges.visibility = View.GONE
        } else {
            searchItem.itemEpisodeBadges.visibility =
                View.VISIBLE
            searchItem.itemEpisodeBadges.text = data.episodeBadges
        }
        searchItem.itemSearchEpisodeAction.setOnClickListener {
            goToSeason(data)
        }
        val requestOptions = RequestOptions()
            .placeholder(R.drawable.pic_doing_h)
            .error(R.drawable.pic_load_failed)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        Glide.with(this@Search)
            .load(data.episodeCover)
            .apply(requestOptions)
            .addListener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ) = false

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any,
                    target: Target<Drawable?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    setAnimateState(false, 400, searchItem.itemSearchEpisodePlaceholder) {
                        setAnimateState(true, 400, searchItem.itemSearchEpisodeCover)
                    }
                    return false
                }
            })
            .into(searchItem.itemSearchEpisodeCover)
        return searchItem.root
    }

    private fun goToSeason(data: SearchData) {
        Season.startActivity(
            this@Search,
            data.seasonTitle.toString(),
            data.seasonId,
            data.seasonCover
        )
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
                        binding.searchHistoryList,
                        false
                    )
                    itemSearchWord.itemWordTitle.text = historyIndex
                    itemSearchWord.itemWordTitle.setOnClickListener {
                        onSearch(historyIndex)
                    }
                    binding.searchHistoryList.addView(itemSearchWord.root, paramsLinear)
                }
                setAnimateState(true, 500, binding.searchHistory)
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

    private var timer: Timer? = null
    private var imageIndex = 0
    private fun startOnLoadingState(imageView: ImageView) {
        imageView.visibility = View.VISIBLE
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                imageIndex =
                    if (imageIndex == R.drawable.pic_search_doing_1) R.drawable.pic_search_doing_2 else R.drawable.pic_search_doing_1
                runOnUiThread { imageView.setImageResource(imageIndex) }
            }
        }, 0, 500)
    }

    private fun stopOnLoadingState() {
        timer?.let {
            it.cancel()
            timer = null
        }
    }

    override fun onBackPressed() {
        if (isSuggesting) {
            isSuggesting = false
            binding.searchEdit.clearFocus()
        } else {
            stopOnLoadingState()
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