package io.github.sgpublic.bilidownload.ui.fragment

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.github.sgpublic.bilidownload.data.SeasonData
import io.github.sgpublic.bilidownload.data.parcelable.EpisodeData
import io.github.sgpublic.bilidownload.fragment.season.SeasonEpisode
import io.github.sgpublic.bilidownload.fragment.season.SeasonInfo

class SeasonFragmentAdapter(
    private val context: AppCompatActivity, private val seasonData: SeasonData,
    private val episodeData: List<EpisodeData>
) : FragmentStateAdapter(context) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> SeasonInfo(context, seasonData)
            1 -> SeasonEpisode(context, episodeData)
            else -> throw NullPointerException()
        }
    }
}