package io.github.sgpublic.bilidownload.app.fragment.home

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import io.github.sgpublic.bilidownload.app.activity.Search
import io.github.sgpublic.bilidownload.app.ui.recycler.HomeRecyclerAdapter
import io.github.sgpublic.bilidownload.app.viewmodel.HomeBangumiModel
import io.github.sgpublic.bilidownload.base.app.BaseViewModelFragment
import io.github.sgpublic.bilidownload.core.exsp.TokenPreference
import io.github.sgpublic.bilidownload.core.util.dp
import io.github.sgpublic.bilidownload.core.util.log
import io.github.sgpublic.bilidownload.databinding.FragmentHomeBangumiBinding
import io.github.sgpublic.exsp.ExPreference
import java.util.*

class HomeBangumi(context: AppCompatActivity): BaseViewModelFragment<FragmentHomeBangumiBinding, HomeBangumiModel>(context) {
    private val accessKey: String by lazy { return@lazy ExPreference.get<TokenPreference>().accessToken }

    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {
        ViewModel.getBannerInfo(accessKey)
        ViewModel.getBangumiItems(accessKey, true)
    }

    override fun onCreateViewBinding(container: ViewGroup?): FragmentHomeBangumiBinding =
        FragmentHomeBangumiBinding.inflate(layoutInflater, container, false)

    private val HomeAdapter: HomeRecyclerAdapter by lazy { return@lazy HomeRecyclerAdapter(getContext()) }
    override fun onViewSetup() {
        ViewBinding.bangumiSearch.setOnClickListener {
            Search.startActivity(context)
        }
        ViewBinding.bangumiRefresh.setProgressViewOffset(false, 40.dp, 125.dp)
        ViewBinding.bangumiRefresh.setOnRefreshListener {
            Timer().schedule(object : TimerTask(){
                override fun run() {

                }
            }, 1000)
        }
        ViewBinding.bangumiRecycler.adapter = HomeAdapter
    }

    override fun onViewModelSetup() {
        ViewModel.BannerInfo.observe(this) {
            log.debug("banner size: ${it.size}")
            HomeAdapter.setBannerData(it)
        }
        ViewModel.BangumiItems.observe(this) {
            log.debug("bangumi size: ${it.second.size}")
            HomeAdapter.setBangumiData(it.second, it.first)
        }
    }

    override val ViewModel: HomeBangumiModel by viewModels()

    override fun onPause() {
        HomeAdapter.setBannerLoop(false)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        HomeAdapter.setBannerLoop(true)
    }
}