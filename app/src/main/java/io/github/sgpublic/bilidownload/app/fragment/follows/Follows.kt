package io.github.sgpublic.bilidownload.app.fragment.follows

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import io.github.sgpublic.bilidownload.app.activity.SeasonPlayer
import io.github.sgpublic.bilidownload.app.ui.recycler.FollowsRecyclerAdapter
import io.github.sgpublic.bilidownload.app.viewmodel.FollowModel
import io.github.sgpublic.bilidownload.base.app.BaseViewModelFragment
import io.github.sgpublic.bilidownload.databinding.FragmentFollowsBinding

class Follows(
    context: AppCompatActivity, private val status: FollowModel.FollowStatus
) : BaseViewModelFragment<FragmentFollowsBinding, FollowModel>(context) {
    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {

    }

    private val adapter: FollowsRecyclerAdapter by lazy {
        FollowsRecyclerAdapter().also {
            it.setOnScrollToEndListener {
                ViewModel.getFollows(false)
            }
            it.setOnEpisodeClickListener { sid ->
                SeasonPlayer.startActivity(context, sid)
            }
        }
    }
    override fun onViewSetup() {
        ViewBinding.followsRecycler.adapter = adapter
        ViewBinding.root.setOnRefreshListener {
            ViewModel.getFollows(true)
        }
    }

    override fun onViewModelSetup() {
        ViewModel.Follows.observe(this) {
            ViewModel.Loading.postValue(false)
            adapter.setFollowsData(it.first, it.second)
        }
        ViewModel.Loading.observe(this) {
            ViewBinding.root.isRefreshing = it
        }
    }

    override fun getTitle(): CharSequence = context.getString(status.title)

    override fun onCreateViewBinding(container: ViewGroup?): FragmentFollowsBinding =
        FragmentFollowsBinding.inflate(layoutInflater, container, false)
    override val ViewModel: FollowModel by viewModels { FollowModel.Factory(status) }
}