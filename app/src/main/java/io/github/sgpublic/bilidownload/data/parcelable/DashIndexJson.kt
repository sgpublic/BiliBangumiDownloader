package io.github.sgpublic.bilidownload.data.parcelable

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Suppress("PropertyName", "SpellCheckingInspection")
@Parcelize
data class DashIndexJson(
    var video: DASHData = DASHData(),
    var audio: DASHData = DASHData(),
) : Parcelable, Serializable {

    @Parcelize
    data class DASHData(
        var id: Int = 0,
        var base_url: String = "",
        var backup_url: ArrayList<String> = arrayListOf(),
        var bandwidth: Long = 0L,
        var codecid: Int = 0,
        var size: Long = 0L,
        var md5: String = "",
        var no_rexcode: Boolean = false,
        var frame_rate: String = "",
        var width: Int = 0,
        var height: Int = 0
    ): Parcelable, Serializable {
    }
}