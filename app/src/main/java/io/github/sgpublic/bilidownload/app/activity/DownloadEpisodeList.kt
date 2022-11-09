package io.github.sgpublic.bilidownload.app.activity

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import io.github.sgpublic.bilidownload.base.app.BaseActivity
import io.github.sgpublic.bilidownload.databinding.ActivityRecyclerBinding

/**
 *
 * @author Madray Haven
 * @date 2022/11/9 10:53
 */
class DownloadEpisodeList: BaseActivity<ActivityRecyclerBinding>() {
    override fun onActivityCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onViewSetup() {
        setSupportActionBar(ViewBinding.recyclerToolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            title = intent.getStringExtra(KEY_SEASON_TITLE) ?: ""
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return false
    }

    override val ViewBinding: ActivityRecyclerBinding by viewBinding()

    companion object {
        const val KEY_SEASON_ID = "season_id"
        const val KEY_SEASON_TITLE = "season_title"

        fun startActivity(origin: Context, sid: Long, title: String) {
            origin.startActivity(Intent(origin, DownloadEpisodeList::class.java).also {
                it.putExtra(KEY_SEASON_ID, sid)
                it.putExtra(KEY_SEASON_TITLE, title)
            })
        }
    }
}