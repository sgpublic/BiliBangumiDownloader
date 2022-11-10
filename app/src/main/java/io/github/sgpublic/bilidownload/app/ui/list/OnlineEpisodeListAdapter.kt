package io.github.sgpublic.bilidownload.app.ui.list

import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp.SeasonInfoData

class OnlineEpisodeListAdapter:
    PlayerEpisodeListAdapter<SeasonInfoData.Episodes.EpisodesData.EpisodesItem>() {
    override fun getItemTitle(data: SeasonInfoData.Episodes.EpisodesData.EpisodesItem): String {
        return data.longTitle.ifBlank {
            try {
                Application.getString(
                    R.string.text_episode_index,
                    data.title.apply { toFloat() })
            } catch (_: NumberFormatException) {
                data.title
            }
        }
    }

    override fun isSelected(data: SeasonInfoData.Episodes.EpisodesData.EpisodesItem): Boolean {
        return getSelectedItem().id == data.id
    }
}