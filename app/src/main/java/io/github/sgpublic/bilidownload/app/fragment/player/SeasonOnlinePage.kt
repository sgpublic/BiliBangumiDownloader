package io.github.sgpublic.bilidownload.app.fragment.player

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.lxj.xpopup.XPopup
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.app.dialog.SeasonInfoDialog
import io.github.sgpublic.bilidownload.app.ui.list.RecommendAdapter
import io.github.sgpublic.bilidownload.app.ui.list.SeriesListAdapter
import io.github.sgpublic.bilidownload.app.viewmodel.OnlinePlayerModel
import io.github.sgpublic.bilidownload.base.app.BaseViewModelFragment
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp
import io.github.sgpublic.bilidownload.core.forest.find
import io.github.sgpublic.bilidownload.core.util.IntentUtil.openBiliComic
import io.github.sgpublic.bilidownload.core.util.IntentUtil.openBrowser
import io.github.sgpublic.bilidownload.databinding.FragmentSeasonOnlineBinding

/**
 *
 * @author Madray Haven
 * @date 2022/10/27 15:26
 */
class SeasonOnlinePage(activity: AppCompatActivity): BaseViewModelFragment<FragmentSeasonOnlineBinding, OnlinePlayerModel>(activity) {
    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {

    }

    private val seriesAdapter by lazy {
        SeriesListAdapter(context).also {
            it.setOnItemClickListener { sid ->
                ViewModel.SID.postValue(sid)
            }
        }
    }
    private val recommendAdapter by lazy {
        RecommendAdapter(context).also { adapter ->
            adapter.setOnResourceItemClickListener { data ->
                XPopup.Builder(context).asConfirm(
                    Application.getString(R.string.title_season_recommend_comic),
                    Application.getString(R.string.text_season_recommend_comic)
                ) {
                    // TODO 打开 哔哩漫画 app
                    openBrowser(data)
                }.show()
            }
        }
    }
    override fun onViewSetup() {
        ViewBinding.seasonRecommendComic.adapter = seriesAdapter
    }

    override fun onViewModelSetup() {
        ViewModel.SeasonData.observe(this) { season ->
            ViewModel.Loading.postValue(false)
            ViewBinding.seasonDetailCover.setOnClickListener {
                XPopup.Builder(context)
                    .asCustom(SeasonInfoDialog(context, season))
                    .show()
            }
            val seriesList = ArrayList<SeasonInfoResp.SeasonInfoData.Seasons.SeasonData.SeasonItem>()
            for (item in season.find<SeasonInfoResp.SeasonInfoData.Seasons>()) {
                seriesList.addAll(item.data.seasons)
            }
            if (seriesList.isNotEmpty()) {
                ViewBinding.seasonSeriesTitle.visibility = View.VISIBLE
            }
            seriesAdapter.setData(seriesList)
            ViewBinding.seasonTitle.text = season.seasonTitle
            ViewBinding.seasonDetail.text = season.typeDesc
            ViewBinding.seasonChoseEpisodeText.text = season.newEp.desc
        }
        ViewModel.SeasonRecommend.observe(this) {
            ViewBinding.seasonRecommendTitle.visibility = View.VISIBLE
            recommendAdapter.setData(it.cards)
        }
    }

    override val ViewModel: OnlinePlayerModel by activityViewModels()
    override fun onCreateViewBinding(container: ViewGroup?) =
        FragmentSeasonOnlineBinding.inflate(layoutInflater, container, false)
}