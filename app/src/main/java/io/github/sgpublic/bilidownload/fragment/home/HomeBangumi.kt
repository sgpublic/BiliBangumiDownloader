package io.github.sgpublic.bilidownload.fragment.home

import android.content.res.Configuration
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.activity.Search
import io.github.sgpublic.bilidownload.base.BaseFragment
import io.github.sgpublic.bilidownload.core.data.FollowData
import io.github.sgpublic.bilidownload.core.manager.ConfigManager
import io.github.sgpublic.bilidownload.core.module.FollowsModule
import io.github.sgpublic.bilidownload.core.util.dp
import io.github.sgpublic.bilidownload.databinding.FragmentHomeBangumiBinding
import io.github.sgpublic.bilidownload.ui.SeasonBannerAdapter
import io.github.sgpublic.bilidownload.ui.recycler.HomeRecyclerAdapter
import java.util.*

class HomeBangumi(context: AppCompatActivity): BaseFragment<FragmentHomeBangumiBinding>(context) {
    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {
        getFollowData(1)
    }

    override fun onCreateViewBinding(container: ViewGroup?): FragmentHomeBangumiBinding =
        FragmentHomeBangumiBinding.inflate(layoutInflater, container, false)

    private lateinit var adapter: HomeRecyclerAdapter
    override fun onViewSetup() {
        initViewAtTop(ViewBinding.bangumiSearchBase)
        ViewBinding.bangumiSearch.setOnClickListener {
            Search.startActivity(context)
        }
        ViewBinding.bangumiRefresh.setProgressViewOffset(false, 40.dp, 125.dp)
        ViewBinding.bangumiRefresh.setOnRefreshListener {
            Timer().schedule(object : TimerTask(){
                override fun run() {
                    getFollowData(1)
                }
            }, 1000)
        }
        adapter = HomeRecyclerAdapter(context)
        adapter.setOnScrollToEndListener {
            getFollowData(it)
        }
        ViewBinding.bangumiRecycler.adapter = adapter
    }

    private fun getFollowData(pageIndex: Int) {
        if (pageIndex == 1) {
            runOnUiThread {
                adapter.removeAllFollows()
            }
        }
        val accessKey = ConfigManager.ACCESS_TOKEN
        val mid = ConfigManager.MID
        val helper = FollowsModule(accessKey)
        helper.getFollows(mid, pageIndex, 2, object : FollowsModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                Application.onToast(context, R.string.error_bangumi_load, message, code)
                runOnUiThread {
                    ViewBinding.bangumiLoadState.stopLoad(true)
                    ViewBinding.bangumiRefresh.isRefreshing = false
                }
            }

            override fun onResult(followData: ArrayList<FollowData>, hasNext: Boolean) {
                runOnUiThread {
                    if (followData.isEmpty()) {
                        ViewBinding.bangumiLoadState.loadEmpty()
                        ViewBinding.bangumiRefresh.isRefreshing = false
                    } else {
                        ViewBinding.bangumiLoadState.stopLoad()
                        ViewBinding.bangumiRecycler.visibility = View.VISIBLE
                        if (pageIndex == 1) {
                            adapter.setBannerData(getBannerData(followData))
                        }
                        adapter.insertFollowData(followData, hasNext)
                        ViewBinding.bangumiRefresh.isRefreshing = false
                    }
                }
            }
        })
    }

    private fun getBannerData(dataArray: ArrayList<FollowData>): ArrayList<SeasonBannerAdapter.BannerItem> {
        val bannerInfoList: ArrayList<SeasonBannerAdapter.BannerItem> = ArrayList()
        val nightMode = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        for (data in dataArray) {
            if (data.isFinish != 0 && data.newEpIsNew != 1) {
                continue
            }
            val badgeColor: Int = if (nightMode) {
                data.badgeColorNight
            } else {
                data.badgeColor
            }
            bannerInfoList.add(
                SeasonBannerAdapter.BannerItem(
                    data.newEpCover, data.cover, data.seasonId, data.title,
                    data.newEpIndexShow, data.badge, badgeColor
                )
            )
            if (bannerInfoList.size > 7) {
                break
            }
        }
        val bannerItemCount = dataArray.size.coerceAtMost(5)
        while (bannerInfoList.size < bannerItemCount) {
            val data = dataArray[(Math.random() * dataArray.size).toInt()]
            if (data.isFinish != 1) {
                continue
            }
            var isEquals = false
            for (item_index in bannerInfoList) {
                if (item_index.seasonId == data.seasonId) {
                    isEquals = true
                    break
                }
            }
            if (isEquals) {
                continue
            }
            val badgeColor: Int = if (nightMode) {
                data.badgeColorNight
            } else {
                data.badgeColor
            }
            bannerInfoList.add(
                SeasonBannerAdapter.BannerItem(
                    data.newEpCover, data.cover, data.seasonId, data.title,
                    data.newEpIndexShow, data.badge, badgeColor
                )
            )
        }
        return bannerInfoList
    }

    override fun onPause() {
        super.onPause()
        adapter.setBannerLoop(false)
    }

    override fun onResume() {
        super.onResume()
        adapter.setBannerLoop(true)
    }
}