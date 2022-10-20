package io.github.sgpublic.bilidownload.core.forest.core

import com.dtflys.forest.exceptions.ForestRuntimeException
import com.google.gson.JsonObject

class BiliApiException(val code: Int, val msg: String, val body: JsonObject):
    ForestRuntimeException("$msg ($code)") {
    override val message: String? get() = super.message
}