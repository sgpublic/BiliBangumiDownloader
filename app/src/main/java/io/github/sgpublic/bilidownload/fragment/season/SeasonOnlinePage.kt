package io.github.sgpublic.bilidownload.fragment.season

import android.Manifest
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.BaseViewModelFragment
import io.github.sgpublic.bilidownload.core.data.ComicData
import io.github.sgpublic.bilidownload.core.manager.ConfigManager
import io.github.sgpublic.bilidownload.core.util.IntentUtil
import io.github.sgpublic.bilidownload.core.util.newObserve
import io.github.sgpublic.bilidownload.databinding.FragmentSeasonOnlineBinding
import io.github.sgpublic.bilidownload.dialog.EpisodeListDialog
import io.github.sgpublic.bilidownload.dialog.SAFNoticeDialog
import io.github.sgpublic.bilidownload.dialog.SeasonInfoDialog
import io.github.sgpublic.bilidownload.room.entity.TaskEntity
import io.github.sgpublic.bilidownload.ui.customLoad
import io.github.sgpublic.bilidownload.ui.list.RecommendComicListAdapter
import io.github.sgpublic.bilidownload.ui.list.RecommendSeasonListAdapter
import io.github.sgpublic.bilidownload.ui.list.SeriesListAdapter
import io.github.sgpublic.bilidownload.ui.recycler.EpisodeRecyclerAdapter
import io.github.sgpublic.bilidownload.ui.smartDestroy
import io.github.sgpublic.bilidownload.ui.withCrossFade
import io.github.sgpublic.bilidownload.viewmodel.OnlinePlayerViewModel

class SeasonOnlinePage(context: AppCompatActivity)
    : BaseViewModelFragment<FragmentSeasonOnlineBinding, OnlinePlayerViewModel>(context) {
    override val ViewModel: OnlinePlayerViewModel by activityViewModels()

    private lateinit var dialog: BasePopupView
    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {
        val notice = SAFNoticeDialog(context)
        dialog = XPopup.Builder(context)
            .asCustom(notice)
//        val result = registerForActivityResult(object : ActivityResultContract<Uri, Uri>() {
//            override fun createIntent(context: Context, input: Uri): Intent {
//                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
//                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
//                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
//                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
//                        Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
//                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, input)
//                return intent
//            }
//
//            override fun parseResult(resultCode: Int, intent: Intent?): Uri {
//                return intent?.data ?: Uri.EMPTY
//            }
//        }) { uri ->
//            if (uri == Uri.EMPTY) {
//                return@registerForActivityResult
//            }
//            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//            Application.CONTENT_RESOLVER.takePersistableUriPermission(uri, takeFlags)
//            notice.smartDestroy()
//        }
        val register = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            for ((_, bool) in it) {
                if (!bool) return@registerForActivityResult
            }
            dialog.smartDestroy()
            Application.startListenTask()
        }
        notice.setOnConfirmListener {
            register.launch(arrayListOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).also {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    it.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray())
        }
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
                val view = EpisodeListDialog(
                    context, list, ViewModel.PLAYER_EPISODE_INDEX,
                    Application.getString(
                        R.string.text_player_dialog_episode, list.size
                    ), ViewModel.getQualityMap()
                )
                view.setOnItemSelectListener { list, qn ->
                    Thread {
                        val qnId = ViewModel.getQualityList()[qn].key
                        val sid = ViewModel.getSeasonId()
                        val tasks = list.map { episode ->
                            TaskEntity(
                                episode.cid, sid,
                                entry = episode.toEntryJson(
                                    ViewModel.getSeriesData()!!
                                ).also {
                                    it.season_id = ViewModel.getSeasonId()
                                    it.video_quality = qnId
                                },
                            )
                        }
                        Application.DATABASE.TasksDao().save(tasks)
                        Application.startListenTask()
                    }.run()
                }
                view.setBeforeEnterToSelectMod before@{
//                    if (context.checkSafPermission(ConfigManager.BASE_DIR)) {
//                        return@before false
//                    }
                    val base = ConfigManager.BASE_DIR
                    if (base.canWrite() && base.canRead()) {
                        return@before false
                    }
                    dialog.show()
                    return@before true
                }
                val popup = XPopup.Builder(context)
                    .asCustom(view)
                    .show()
                view.setOnItemClickListener { index ->
                    popup.dismissWith {
                        popup.destroy()
                    }
                    ViewModel.requestPlayEpisode(index)
                }
            }
            ViewBinding.seasonEpisodeList.adapter = EpisodeRecyclerAdapter(context, list,
                ViewModel.PLAYER_EPISODE_INDEX).also {
                it.setItemClickListener { index ->
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
            Glide.with(context)
                .customLoad(season.producerAvatar)
                .withCrossFade()
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
                    adapter.setOnItemClickListener { data ->
                        XPopup.Builder(context).asConfirm(
                            Application.getString(R.string.title_season_recommend_comic),
                            Application.getString(R.string.text_season_recommend_comic)
                        ) { openBiliComic(data) }.show()
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

    private fun openBiliComic(comic: ComicData) {
        try {
            IntentUtil.openBiliComic(comic.item_id)
        } catch (_: Exception) {
            IntentUtil.openBrowser(comic.url)
        }
    }

    private fun postSeasonId(sid: Long) {
        ViewModel.SEASON_ID.postValue(sid)
    }

    override fun onCreateViewBinding(container: ViewGroup?): FragmentSeasonOnlineBinding =
        FragmentSeasonOnlineBinding.inflate(layoutInflater, container, false)
}