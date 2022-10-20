package io.github.sgpublic.bilidownload.app.fragment.home

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import io.github.sgpublic.bilidownload.app.activity.Search
import io.github.sgpublic.bilidownload.base.app.BaseFragment
import io.github.sgpublic.bilidownload.core.util.dp
import io.github.sgpublic.bilidownload.databinding.FragmentHomeBangumiBinding
import java.util.*

class HomeBangumi(context: AppCompatActivity): BaseFragment<FragmentHomeBangumiBinding>(context) {
    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {
        getFollowData(1)
    }

    override fun onCreateViewBinding(container: ViewGroup?): FragmentHomeBangumiBinding =
        FragmentHomeBangumiBinding.inflate(layoutInflater, container, false)

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
    }

    private fun getFollowData(pageIndex: Int) {
        if (pageIndex == 1) {
            runOnUiThread {

            }
        }
    }
}