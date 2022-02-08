package io.github.sgpublic.bilidownload.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.android.exoplayer2.Player
import io.github.sgpublic.bilidownload.base.BasePlayer
import io.github.sgpublic.bilidownload.data.parcelable.EntryJson

@Suppress("PrivatePropertyName")
class LocalPlayer: BasePlayer<EntryJson>(), Player.Listener {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        binding.playerControllerQuality.visibility = View.GONE
    }

    override fun onPlayEpisode(data: EntryJson) {
        // TODO
    }

    override fun onMapEpisodeList(data: EntryJson): String =
        "${data.ep.index} ${data.ep.index_title}"

    companion object {
        fun startActivity(context: Context, entryList: Array<EntryJson>, pos: Int) {
            val intent = Intent(context, LocalPlayer::class.java)
            intent.putExtra(KEY_LIST, entryList)
            intent.putExtra(KEY_POSITION, pos)
            context.startActivity(intent)
        }
    }
}