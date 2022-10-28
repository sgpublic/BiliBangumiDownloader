package io.github.sgpublic.bilidownload.app.fragment.player

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.lxj.xpopup.XPopup
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.app.dialog.SeasonInfoDialog
import io.github.sgpublic.bilidownload.app.ui.list.EpisodeListAdapter
import io.github.sgpublic.bilidownload.app.ui.list.RecommendAdapter
import io.github.sgpublic.bilidownload.app.ui.list.SeriesListAdapter
import io.github.sgpublic.bilidownload.app.ui.recycler.SeasonEpisodeAdapter
import io.github.sgpublic.bilidownload.app.viewmodel.OnlinePlayerModel
import io.github.sgpublic.bilidownload.base.app.BaseViewModelFragment
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp
import io.github.sgpublic.bilidownload.core.forest.find
import io.github.sgpublic.bilidownload.core.util.IntentUtil.openUrl
import io.github.sgpublic.bilidownload.databinding.FragmentSeasonOnlineBinding

/**
 *
 * @author Madray Haven
 * @date 2022/10/27 15:26
 */
class SeasonOnlinePage(activity: AppCompatActivity): BaseViewModelFragment<FragmentSeasonOnlineBinding, OnlinePlayerModel>(activity) {
    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {

    }

    private val episodeAdapter by lazy {
        SeasonEpisodeAdapter().also {
            it.setOnItemClickListener { epid ->

            }
        }
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
                    Application.getString(R.string.title_open_other),
                    Application.getString(R.string.text_open_other)
                ) {
                    openUrl(data)
                }.show()
            }
        }
    }
    override fun onViewSetup() {
        ViewBinding.seasonEpisodeList.adapter = episodeAdapter
        ViewBinding.seasonSeries.adapter = seriesAdapter
        ViewBinding.seasonRecommend.adapter = recommendAdapter
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
                seriesAdapter.setData(seriesList)
            }
            ViewBinding.seasonTitle.text = season.seasonTitle
            ViewBinding.seasonDetail.text = season.typeDesc
            ViewBinding.seasonChoseEpisodeText.text = season.newEp.desc

            val episodeList = ArrayList<SeasonInfoResp.SeasonInfoData.Episodes.EpisodesData.EpisodesItem>()
            for (item in season.find<SeasonInfoResp.SeasonInfoData.Episodes>()) {
                episodeList.addAll(item.data.episodes)
            }
            if (episodeList.isNotEmpty()) {
                episodeAdapter.setData(episodeList)
            }
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