package io.github.sgpublic.bilidownload.app.activity

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import com.google.android.material.tabs.TabLayoutMediator
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.app.fragment.follows.Follows
import io.github.sgpublic.bilidownload.app.ui.fragment.FollowsFragmentAdapter
import io.github.sgpublic.bilidownload.app.viewmodel.FollowModel
import io.github.sgpublic.bilidownload.base.app.BaseActivity
import io.github.sgpublic.bilidownload.databinding.ActivityMyFollowsBinding

class MyFollows: BaseActivity<ActivityMyFollowsBinding>() {
    override fun onActivityCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onViewSetup() {
        setSupportActionBar(ViewBinding.myFollowsToolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.title_mine_my_follows)
        }

        val list = ArrayList<Follows>()
        list.add(Follows(this@MyFollows, FollowModel.FollowStatus.Want))
        list.add(Follows(this@MyFollows, FollowModel.FollowStatus.Watching))
        list.add(Follows(this@MyFollows, FollowModel.FollowStatus.Watched))
        ViewBinding.myFollowsPager.adapter = FollowsFragmentAdapter(this@MyFollows, list)
        TabLayoutMediator(ViewBinding.myFollowsTab, ViewBinding.myFollowsPager) { tab, pos ->
            tab.text = list[pos].getTitle()
        }.attach()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return false
    }

    override val ViewBinding: ActivityMyFollowsBinding by viewBinding()
    companion object {
        fun startActivity(context: Context){
            val intent = Intent(context, MyFollows::class.java)
            context.startActivity(intent)
        }
    }
}