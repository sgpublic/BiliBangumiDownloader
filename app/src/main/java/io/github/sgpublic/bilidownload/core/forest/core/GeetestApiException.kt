package io.github.sgpublic.bilidownload.core.forest.core

import com.dtflys.forest.exceptions.ForestRuntimeException
import com.google.gson.JsonObject

class GeetestApiException(override val message: String): ForestRuntimeException(message)