package io.github.sgpublic.bilidownload.app.ui.list

import io.github.sgpublic.bilidownload.core.room.entity.DownloadTaskEntity

class LocalEpisodeListAdapter:
    PlayerEpisodeListAdapter<DownloadTaskEntity>() {
    override fun getItemTitle(data: DownloadTaskEntity): String {
        return data.episodeTitle
    }

    override fun isSelected(data: DownloadTaskEntity): Boolean {
        return getSelectedItem().epid == data.epid
    }

    override fun getItemId(position: Int) = getItem(position).epid
}