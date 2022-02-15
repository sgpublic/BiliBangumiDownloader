package io.github.sgpublic.bilidownload.room.converter

import androidx.room.TypeConverter
import io.github.sgpublic.bilidownload.base.BaseConverter
import io.github.sgpublic.bilidownload.data.parcelable.DashIndexJson

class DashIndexJsonConverter: BaseConverter<DashIndexJson, String> {
    @TypeConverter
    override fun encode(obj: DashIndexJson): String {
        return obj.toJson()
    }
    @TypeConverter
    override fun decode(value: String): DashIndexJson {
        return DashIndexJson.fromStr(value)
    }
}