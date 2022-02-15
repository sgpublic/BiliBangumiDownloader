package io.github.sgpublic.bilidownload.activity

import android.content.Context
import android.content.Intent
import com.google.android.material.tabs.TabLayoutMediator
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.BaseActivity
import io.github.sgpublic.bilidownload.databinding.ActivityMyFollowsBinding
import io.github.sgpublic.bilidownload.fragment.follows.Follows
import io.github.sgpublic.bilidownload.module.FollowsModule
import io.github.sgpublic.bilidownload.ui.fragment.FollowsFragmentAdapter

class MyFollows: BaseActivity<ActivityMyFollowsBinding>() {
    override fun onActivityCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onCreateViweBinding(): ActivityMyFollowsBinding =
        ActivityMyFollowsBinding.inflate(layoutInflater)

    override fun onViewSetup() {
        setSupportActionBar(ViewBinding.myFollowsToolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.title_mine_my_follows)
        }

        val list = ArrayList<Follows>()
        list.add(Follows(this@MyFollows, R.string.title_follows_want, FollowsModule.STATUS_WANT))
        list.add(Follows(this@MyFollows, R.string.title_follows_watching, FollowsModule.STATUS_WATCHING))
        list.add(Follows(this@MyFollows, R.string.title_follows_has_watched, FollowsModule.STATUS_HAS_WATCHED))
        ViewBinding.myFollowsPager.adapter = FollowsFragmentAdapter(this@MyFollows, list)
        TabLayoutMediator(ViewBinding.myFollowsTab, ViewBinding.myFollowsPager) { tab, pos ->
            tab.text = list[pos].getTitle()
        }.attach()
    }

    companion object {
        fun startActivity(context: Context){
            val intent = Intent(context, MyFollows::class.java)
            context.startActivity(intent)
        }
    }
}