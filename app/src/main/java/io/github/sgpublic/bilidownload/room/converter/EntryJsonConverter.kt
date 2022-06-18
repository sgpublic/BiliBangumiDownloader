package io.github.sgpublic.bilidownload.room.converter

import androidx.room.TypeConverter
import io.github.sgpublic.bilidownload.base.BaseConverter
import io.github.sgpublic.bilidownload.core.data.parcelable.EntryJson

class EntryJsonConverter: BaseConverter<EntryJson?, String> {
    @TypeConverter
    override fun encode(obj: EntryJson?): String {
        return obj?.toJson() ?: ""
    }
    @TypeConverter
    override fun decode(value: String): EntryJson? {
        return if (value == "") null else EntryJson.fromStr(value)
    }
}