package io.github.sgpublic.bilidownload.app.fragment.home

import android.os.Handler
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import io.github.sgpublic.bilidownload.app.activity.Search
import io.github.sgpublic.bilidownload.app.activity.SeasonPlayer
import io.github.sgpublic.bilidownload.app.ui.recycler.HomeRecyclerAdapter
import io.github.sgpublic.bilidownload.app.viewmodel.HomeModel
import io.github.sgpublic.bilidownload.base.app.BaseViewModelFragment
import io.github.sgpublic.bilidownload.core.util.dp
import io.github.sgpublic.bilidownload.core.util.log
import io.github.sgpublic.bilidownload.databinding.FragmentHomeBangumiBinding
import java.util.*

class HomeBangumi(context: AppCompatActivity): BaseViewModelFragment<FragmentHomeBangumiBinding, HomeModel>(context) {
    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onCreateViewBinding(container: ViewGroup?): FragmentHomeBangumiBinding =
        FragmentHomeBangumiBinding.inflate(layoutInflater, container, false)

    private val HomeAdapter: HomeRecyclerAdapter by lazy { HomeRecyclerAdapter() }
    override fun onViewSetup() {
        ViewBinding.bangumiSearch.setOnClickListener {
            Search.startActivity(context)
        }
        ViewBinding.bangumiRefresh.setProgressViewOffset(false, 40.dp, 125.dp)
        ViewBinding.bangumiRefresh.setOnRefreshListener {
            postDelayed(1000) {
                ViewModel.getBannerInfo()
                ViewModel.getBangumiItems(true)
            }
        }
        ViewBinding.bangumiRecycler.adapter = HomeAdapter
        HomeAdapter.setOnEpisodeClickListener { sid, epid ->
            SeasonPlayer.startActivity(context, sid, epid)
        }
        HomeAdapter.setOnScrollToEndListener {
            ViewModel.getBangumiItems(false)
        }
    }

    override fun onViewModelSetup() {
        ViewModel.BannerInfo.observe(this) {
            log.debug("banner size: ${it.size}")
            HomeAdapter.setBannerData(it)
            ViewModel.Loading.postValue(false)
        }
        ViewModel.BangumiItems.observe(this) {
            log.debug("bangumi size: ${it.second.size}")
            HomeAdapter.setBangumiData(it.second, it.first)
            ViewModel.Loading.postValue(false)
        }
        ViewModel.Loading.observe(this) {
            ViewBinding.bangumiRefresh.isRefreshing = it
        }
    }

    override val ViewModel: HomeModel by activityViewModels()

    override fun onPause() {
        HomeAdapter.setBannerLoop(false)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        HomeAdapter.setBannerLoop(true)
    }
}