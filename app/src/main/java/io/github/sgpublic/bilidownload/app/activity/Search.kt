package io.github.sgpublic.bilidownload.app.activity

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.widget.addTextChangedListener
import io.github.sgpublic.bilidownload.base.app.BaseActivity
import io.github.sgpublic.bilidownload.core.util.log
import io.github.sgpublic.bilidownload.databinding.ActivitySearchBinding
import io.github.sgpublic.bilidownload.databinding.ItemSearchWordBinding
import org.json.JSONArray
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader


class Search: BaseActivity<ActivitySearchBinding>() {
    override fun onActivityCreated(hasSavedInstanceState: Boolean) {
        getHistory()

    }

    override fun onViewSetup() {
        ViewBinding.searchSuggestionCover.setOnClickListener {
            ViewBinding.searchEdit.clearFocus()
        }
        ViewBinding.searchBack.setOnClickListener {
            onBackPressed()
        }
        ViewBinding.searchEdit.addTextChangedListener(afterTextChanged = {
            if (ViewBinding.searchEdit.hasFocus()){

            }
        })
        ViewBinding.searchEdit.setOnEditorActionListener listener@{ _, id, _ ->
            if (id == EditorInfo.IME_ACTION_SEARCH) {
                ViewBinding.searchEdit.clearFocus()
                onSearch()
                return@listener true
            }
            return@listener false
        }
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

        }
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
        } catch (e: Exception) {
            if (e !is FileNotFoundException) {
                log.warn("history read failed", e)
            }
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
            } catch (ignore: FileNotFoundException) { }
            val fileOutputStream = applicationContext.openFileOutput("history.json", MODE_PRIVATE)
            fileOutputStream.write(arraySave.toString().toByteArray())
            fileOutputStream.close()
        } catch (e: IOException) {
            log.warn("save history failed", e)
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        if (ViewBinding.searchEdit.hasFocus()) {
            ViewBinding.searchEdit.clearFocus()
        } else {
            finish()
        }
    }

    override val ViewBinding: ActivitySearchBinding by viewBinding()

    companion object {
        fun startActivity(context: Context){
            val intent = Intent(context, Search::class.java)
            context.startActivity(intent)
        }
    }
}