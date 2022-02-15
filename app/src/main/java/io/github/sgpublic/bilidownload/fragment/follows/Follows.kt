package io.github.sgpublic.bilidownload.fragment.follows

import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.BaseFragment
import io.github.sgpublic.bilidownload.data.FollowData
import io.github.sgpublic.bilidownload.databinding.FragmentFollowsBinding
import io.github.sgpublic.bilidownload.manager.ConfigManager
import io.github.sgpublic.bilidownload.module.FollowsModule
import io.github.sgpublic.bilidownload.ui.recycler.FollowsRecyclerAdapter
import java.util.*

class Follows(context: AppCompatActivity, @StringRes private val title: Int,
                       private val status: Int) : BaseFragment<FragmentFollowsBinding>(context) {
    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {
        ViewBinding.followsRecycler.visibility = View.INVISIBLE
        ViewBinding.followsLoadState.startLoad()
        getFollowData()
    }

    override fun onCreateViweBinding(container: ViewGroup?): FragmentFollowsBinding =
        FragmentFollowsBinding.inflate(layoutInflater, container, false)

    private lateinit var adapter: FollowsRecyclerAdapter
    override fun onViewSetup() {
        adapter = FollowsRecyclerAdapter(context)
        ViewBinding.followsRefresh.setOnRefreshListener {
            adapter.removeAllFollows()
            Timer().schedule(object : TimerTask(){
                override fun run() {
                    getFollowData()
                }
            }, 500)
        }
        adapter.setOnScrollToEndListener {
            getFollowData(it)
        }
        ViewBinding.followsRecycler.adapter = adapter
    }

    override fun getTitle(): CharSequence = context.getString(title)

    private fun getFollowData(pageIndex: Int = 1) {
        if (pageIndex == 1){
            adapter.removeAllFollows()
        }
        val accessKey = ConfigManager.ACCESS_TOKEN
        val mid = ConfigManager.MID
        val helper = FollowsModule(context, accessKey)
        helper.getFollows(mid, pageIndex, status, object : FollowsModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                Application.onToast(context, R.string.error_bangumi_load, message, code)
                runOnUiThread {
                    ViewBinding.followsLoadState.stopLoad(true)
                    ViewBinding.followsRefresh.isRefreshing = false
                }
            }

            override fun onResult(followData: ArrayList<FollowData>, hasNext: Boolean) {
                runOnUiThread {
                    ViewBinding.followsLoadState.stopLoad()
                    try {
                        if (followData.isEmpty()) {
                            ViewBinding.followsLoadState.setImageResource(R.drawable.pic_null)
                            ViewBinding.followsRefresh.isRefreshing = false
                        } else {
                            ViewBinding.followsLoadState.visibility = View.INVISIBLE
                            ViewBinding.followsRecycler.visibility = View.VISIBLE
                            if (pageIndex == 1) {
                                adapter.removeAllFollows()
                                if (ViewBinding.followsRefresh.isRefreshing){
                                    ViewBinding.followsRefresh.isRefreshing = false
                                }
                            }
                            adapter.insertFollowData(followData, hasNext)
                        }
                    } catch (ignore: NullPointerException) { }
                }
            }
        })
    }
}