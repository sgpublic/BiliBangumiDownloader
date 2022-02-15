package io.github.sgpublic.bilidownload.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
class SeasonData(
    var area: Int = 0,
    var actors: String = "",
    var alias: String = "",
    var evaluate: String = "",
    var staff: String = "",
    var styles: String = "",
    var newEp: String = "",
    var timeLengthShow: String = "",
    var description: String = "",
    var rating: Double = 0.0,
    var actorsLines: Int = 0,
    var staffLines: Int = 0,
    var seasonType: Int = 0,
    var producerAvatar: String = "http://i1.hdslb.com/bfs/face/70fba98159d382c91d236289a3294fb2b0c3f258.jpg",
    var producerName: String = "哔哩哔哩番剧",
    var info: SeriesData = SeriesData(),
    val series: LinkedList<SeriesData> = LinkedList()
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as SeasonData
        return series == that.series
    }

    override fun hashCode(): Int {
        return Objects.hash(series)
    }
}