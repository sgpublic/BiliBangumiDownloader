package io.github.sgpublic.bilidownload.app.fragment.follows

import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import io.github.sgpublic.bilidownload.base.app.BaseFragment
import io.github.sgpublic.bilidownload.databinding.FragmentFollowsBinding

class Follows(context: AppCompatActivity, @StringRes private val title: Int,
                       private val status: Int) : BaseFragment<FragmentFollowsBinding>(context) {
    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {
        ViewBinding.followsRecycler.visibility = View.INVISIBLE
        ViewBinding.followsLoadState.startLoad()
        getFollowData()
    }

    override fun onCreateViewBinding(container: ViewGroup?): FragmentFollowsBinding =
        FragmentFollowsBinding.inflate(layoutInflater, container, false)

    override fun onViewSetup() {

    }

    override fun getTitle(): CharSequence = context.getString(title)

    private fun getFollowData(pageIndex: Int = 1) {

    }
}