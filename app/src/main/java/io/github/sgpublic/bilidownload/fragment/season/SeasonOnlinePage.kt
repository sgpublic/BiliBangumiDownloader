package io.github.sgpublic.bilidownload.fragment.season

import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.lxj.xpopup.XPopup
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.BaseViewModelFragment
import io.github.sgpublic.bilidownload.databinding.FragmentSeasonOnlineBinding
import io.github.sgpublic.bilidownload.dialog.EpisodeListDialog
import io.github.sgpublic.bilidownload.dialog.SeasonInfoDialog
import io.github.sgpublic.bilidownload.ui.list.RecommendComicListAdapter
import io.github.sgpublic.bilidownload.ui.list.RecommendSeasonListAdapter
import io.github.sgpublic.bilidownload.ui.list.SeriesListAdapter
import io.github.sgpublic.bilidownload.ui.recycler.EpisodeRecyclerAdapter
import io.github.sgpublic.bilidownload.util.MyLog
import io.github.sgpublic.bilidownload.util.newObserve
import io.github.sgpublic.bilidownload.viewmodel.OnlinePlayerViewModel

class SeasonOnlinePage(context: AppCompatActivity)
    : BaseViewModelFragment<FragmentSeasonOnlineBinding, OnlinePlayerViewModel>(context) {
    override val ViewModel: OnlinePlayerViewModel by activityViewModels()

    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onViewModelSetup() {
        ViewModel.EPISODE_LIST.newObserve(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                ViewBinding.seasonEpisodeList.visibility = View.GONE
                ViewBinding.seasonChoseEpisode.visibility = View.GONE
                ViewBinding.seasonChoseEpisodeText.visibility = View.GONE
                ViewBinding.seasonChoseEpisodeIc.visibility = View.GONE
                return@newObserve
            }
            ViewBinding.seasonChoseEpisode.setOnClickListener {
                val view = EpisodeListDialog(context, list)
                val popup = XPopup.Builder(context)
                    .asCustom(view)
                    .show()
                view.setOnItemClickLayout { index ->
                    popup.dismiss()
                    ViewModel.requestPlayEpisode(index)
                }
            }
            ViewBinding.seasonEpisodeList.adapter = EpisodeRecyclerAdapter(context, list).also { adapter ->
                adapter.setOnItemClickListener { index ->
                    ViewModel.requestPlayEpisode(index)
                }
            }
        }
        ViewModel.SEASON_DATA.newObserve(viewLifecycleOwner) { season ->
            ViewBinding.seasonDetailCover.setOnClickListener {
                XPopup.Builder(context)
                    .asCustom(SeasonInfoDialog(context, season))
                    .show()
            }
            if (season.series.isNotEmpty()) {
                ViewBinding.seasonSeriesTitle.visibility = View.VISIBLE
                ViewBinding.seasonSeries.adapter = SeriesListAdapter(context, season.series).also { adapter ->
                    adapter.setOnItemClickListener { sid ->
                        postSeasonId(sid)
                    }
                }
            }
            ViewBinding.seasonTitle.text = season.info.title
            Glide.with(context).load(season.producerAvatar)
                .into(ViewBinding.seasonProducerAvatar)
            ViewBinding.seasonProducerName.text = season.producerName
            val detail = if (season.info.badge == "") StringBuilder() else
                StringBuilder(season.info.badge).append("  ")
            detail.append(season.newEp)
            ViewBinding.seasonDetail.text = detail.toString()
            ViewBinding.seasonChoseEpisodeText.text = season.timeLengthShow
        }
        ViewModel.RECOMMEND_COMIC_LIST.newObserve(viewLifecycleOwner) {
            ViewBinding.seasonRecommendTitle.visibility = View.VISIBLE
            ViewBinding.seasonRecommendComic.adapter =
                RecommendComicListAdapter(context, it).also { adapter ->
                    adapter.setOnItemClickListener { id ->
                        XPopup.Builder(context).asConfirm(
                            Application.getString(R.string.title_season_recommend_comic),
                            Application.getString(R.string.text_season_recommend_comic)
                        ) { openBiliComic(id) }.show()
                    }
                }
        }
        ViewModel.RECOMMEND_SEASON_LIST.newObserve(viewLifecycleOwner) {
            ViewBinding.seasonRecommendTitle.visibility = View.VISIBLE
            ViewBinding.seasonRecommendSeason.adapter =
                RecommendSeasonListAdapter(context, it).also { adapter ->
                    adapter.setOnItemClickListener { sid ->
                        postSeasonId(sid)
                    }
                }
        }
    }

    private fun openBiliComic(id: Long) {
        try {
            Intent().apply {
                this.action = action
                this.data = Uri.parse("bilicomic://detail/$id")
            }.let {
                context.startActivity(it)
            }
        } catch (e: Exception) {
            // TODO 提示未安装哔哩漫画
            MyLog.d("哔哩漫画打开失败", e)
        }
    }

    private fun postSeasonId(sid: Long) {
        ViewModel.SEASON_ID.postValue(sid)
    }

    override fun onCreateViweBinding(container: ViewGroup?): FragmentSeasonOnlineBinding =
        FragmentSeasonOnlineBinding.inflate(layoutInflater, container, false)
}