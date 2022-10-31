package io.github.sgpublic.bilidownload.app.fragment.player

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.lxj.xpopup.XPopup
import io.github.sgpublic.bilidownload.app.dialog.SeasonInfoDialog
import io.github.sgpublic.bilidownload.app.ui.recycler.SeasonOnlinePageAdapter
import io.github.sgpublic.bilidownload.app.viewmodel.OnlinePlayerModel
import io.github.sgpublic.bilidownload.base.app.BaseViewModelFragment
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp
import io.github.sgpublic.bilidownload.core.forest.find
import io.github.sgpublic.bilidownload.core.util.*
import io.github.sgpublic.bilidownload.databinding.FragmentSeasonOnlineBinding

/**
 *
 * @author Madray Haven
 * @date 2022/10/27 15:26
 */
class SeasonOnlinePage(activity: AppCompatActivity): BaseViewModelFragment<FragmentSeasonOnlineBinding, OnlinePlayerModel>(activity) {
    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {

    }

    private val adapter: SeasonOnlinePageAdapter by lazy { SeasonOnlinePageAdapter() }
    override fun onViewSetup() {
        ViewBinding.root.adapter = adapter
    }

    override fun onViewModelSetup() {
        ViewModel.SeasonData.observe(this) { season ->
            ViewModel.Loading.postValue(false)

            adapter.setSeasonInfo(season)
            adapter.setOnSeasonDetailClick {
                XPopup.Builder(context)
                    .asCustom(SeasonInfoDialog(context, season))
                    .show()
            }

            val seriesList = ArrayList<SeasonInfoResp.SeasonInfoData.Seasons.SeasonData.SeasonItem>()
            for (item in season.find<SeasonInfoResp.SeasonInfoData.Seasons>()) {
                seriesList.addAll(item.data.seasons)
            }
            adapter.setSeries(seriesList)
            adapter.setOnChoseEpisodeClick {

            }

            adapter.setEpisode(ViewModel.EpisodeList.values)
            adapter.setOnEpisodeItemClickListener { sid, epid ->
                if (sid != ViewModel.SID.value) {
                    ViewModel.SID.postValue(epid)
                }
            }
            adapter.setOnResourceItemClickListener {
                XPopup.Builder(context).showAsOutsideConfirm {
                    IntentUtil.openUrl(it)
                }
            }
        }
        ViewModel.SeasonRecommend.observe(this) {
            adapter.setRecommend(it.cards)
        }
    }

    override val ViewModel: OnlinePlayerModel by activityViewModels()
    override fun onCreateViewBinding(container: ViewGroup?) =
        FragmentSeasonOnlineBinding.inflate(layoutInflater, container, false)
}