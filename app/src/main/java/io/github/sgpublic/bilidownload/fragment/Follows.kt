package io.github.sgpublic.bilidownload.fragment

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.BaseFragment
import io.github.sgpublic.bilidownload.base.CrashHandler
import io.github.sgpublic.bilidownload.data.FollowData
import io.github.sgpublic.bilidownload.databinding.FragmentFollowsBinding
import io.github.sgpublic.bilidownload.manager.ConfigManager
import io.github.sgpublic.bilidownload.module.FollowsModule
import io.github.sgpublic.bilidownload.ui.recycler.FollowsRecyclerAdapter
import java.util.*

class Follows(private val context: AppCompatActivity, @StringRes private val title: Int,
              private val status: Int) : BaseFragment<FragmentFollowsBinding>(context) {
    override fun onFragmentCreated(savedInstanceState: Bundle?) {
        binding.followsRecycler.visibility = View.INVISIBLE
        startOnLoadingState(binding.followsLoadState)
        getFollowData()
    }

    private lateinit var adapter: FollowsRecyclerAdapter
    override fun onViewSetup() {
        adapter = FollowsRecyclerAdapter(context)
        binding.followsRefresh.setOnRefreshListener {
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
        binding.followsRecycler.adapter = adapter
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
                onToast(R.string.error_bangumi_load, message, code)
                runOnUiThread {
                    stopOnLoadingState()
                    binding.followsLoadState.setImageResource(R.drawable.pic_load_failed)
                    binding.followsRefresh.isRefreshing = false
                }
                CrashHandler.saveExplosion(e, code)
            }

            override fun onResult(followData: ArrayList<FollowData>, hasNext: Boolean) {
                runOnUiThread {
                    stopOnLoadingState()
                    try {
                        if (followData.isEmpty()) {
                            binding.followsLoadState.setImageResource(R.drawable.pic_null)
                            binding.followsRefresh.isRefreshing = false
                        } else {
                            binding.followsLoadState.visibility = View.INVISIBLE
                            binding.followsRecycler.visibility = View.VISIBLE
                            if (pageIndex == 1) {
                                adapter.removeAllFollows()
                                if (binding.followsRefresh.isRefreshing){
                                    binding.followsRefresh.isRefreshing = false
                                }
                            }
                            adapter.insertFollowData(followData, hasNext)
                        }
                    } catch (ignore: NullPointerException) { }
                }
            }
        })
    }

    private var timer: MutableMap<ImageView, Timer?> = mutableMapOf()
    private var imageIndex = 0
    private fun startOnLoadingState(image: ImageView) {
        image.visibility = View.VISIBLE
        if (timer[image] == null){
            timer[image] = Timer()
        }
        timer[image]?.schedule(object : TimerTask() {
            override fun run() {
                imageIndex = if (imageIndex == R.drawable.pic_search_doing_1) R.drawable.pic_search_doing_2 else R.drawable.pic_search_doing_1
                runOnUiThread { image.setImageResource(imageIndex) }
            }
        }, 0, 500)
    }

    private fun stopOnLoadingState() {
        for ((key, timer) in timer){
            if (timer != null){
                timer.cancel()
                this.timer[key] = null
            }
        }
    }
}