package io.github.sgpublic.bilidownload.fragment.home

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.activity.Search
import io.github.sgpublic.bilidownload.base.BaseFragment
import io.github.sgpublic.bilidownload.base.CrashHandler
import io.github.sgpublic.bilidownload.data.FollowData
import io.github.sgpublic.bilidownload.databinding.FragmentHomeBangumiBinding
import io.github.sgpublic.bilidownload.manager.ConfigManager
import io.github.sgpublic.bilidownload.module.FollowsModule
import io.github.sgpublic.bilidownload.ui.SeasonBannerAdapter
import io.github.sgpublic.bilidownload.ui.recycler.HomeRecyclerAdapter
import java.util.*

class HomeBangumi(private val context: AppCompatActivity): BaseFragment<FragmentHomeBangumiBinding>(context) {
    override fun onFragmentCreated(savedInstanceState: Bundle?) {
        getFollowData(1)
    }

    private lateinit var adapter: HomeRecyclerAdapter
    override fun onViewSetup() {
        initViewAtTop(binding.bangumiSearchBase)
        binding.bangumiSearch.setOnClickListener {
            Search.startActivity(context)
        }
        binding.bangumiRefresh.setProgressViewOffset(false, Application.dip2px(40F), Application.dip2px(125F))
        binding.bangumiRefresh.setOnRefreshListener {
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
        binding.bangumiRecycler.adapter = adapter
    }

    private fun getFollowData(pageIndex: Int) {
        if (pageIndex == 1) {
            adapter.removeAllFollows()
        }
        val accessKey = ConfigManager.ACCESS_TOKEN
        val mid = ConfigManager.MID
        val helper = FollowsModule(context, accessKey)
        helper.getFollows(mid, pageIndex, object : FollowsModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                onToast(R.string.error_bangumi_load, message, code)
                runOnUiThread {
                    binding.bangumiLoadState.setImageResource(R.drawable.pic_load_failed)
                    binding.bangumiRefresh.isRefreshing = false
                }
                CrashHandler.saveExplosion(e, code)
            }

            override fun onResult(followData: ArrayList<FollowData>, hasNext: Boolean) {
                runOnUiThread {
                    if (followData.isEmpty()) {
                        binding.bangumiLoadState.setImageResource(R.drawable.pic_null)
                        binding.bangumiRefresh.isRefreshing = false
                    } else {
                        binding.bangumiLoadState.visibility = View.INVISIBLE
                        binding.bangumiRecycler.visibility = View.VISIBLE
                        if (pageIndex == 1) {
                            adapter.setBannerData(getBannerData(followData))
                        }
                        adapter.insertFollowData(followData, hasNext)
                        binding.bangumiRefresh.isRefreshing = false
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
                    context, data.newEpCover, data.cover, data.seasonId,
                    data.title, data.newEpIndexShow, data.badge, badgeColor
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
                if (item_index.seasonId != data.seasonId) {
                    continue
                }
                isEquals = true
                break
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
                    context, data.newEpCover, data.cover, data.seasonId,
                    data.title, data.newEpIndexShow, data.badge, badgeColor
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