package io.github.sgpublic.bilidownload.core.forest.core

import com.dtflys.forest.exceptions.ForestRuntimeException

class BiliAccountLimitException(val url: String, override val message: String): ForestRuntimeException(message)