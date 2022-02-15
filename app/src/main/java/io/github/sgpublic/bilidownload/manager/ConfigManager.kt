package io.github.sgpublic.bilidownload.manager

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.BuildConfig
import io.github.sgpublic.bilidownload.data.UserData
import okhttp3.internal.http.toHttpDateString
import java.text.SimpleDateFormat
import java.util.*

object ConfigManager {
    private const val IS_LOGIN_KEY = "is_login"
    private const val MID_KEY = "mid"
    private const val ACCESS_TOKEN_KEY = "access_token"
    private const val REFRESH_TOKEN_KEY = "refresh_token"
    private const val TOKEN_EXPIRE_KEY = "token_expire"

    private const val VIP_STATE_KEY = "vip_state"
    private const val VIP_TYPE_KEY = "vip_type"
    private const val VIP_LABEL_KEY = "vip_label"

    private const val NAME_KEY = "name"
    private const val SIGN_KEY = "sign"
    private const val FACE_KEY = "face"
    private const val SEX_KEY = "sex"
    private const val LEVEL_KEY = "level"

    private const val BASE_DIR_KEY = "base_dir"
    private const val PACKAGE_KEY = "package"
    private const val PLAYER_AUTO_NEXT_KEY = "player_auto_next"
    private const val TASK_AUTO_START_KEY = "task_auto_start"
    private const val TASK_PARALLEL_COUNT_KEY = "task_parallel_count"
    private const val QUALITY_KEY = "quality"

    private const val UPDATE_DATE_KEY = "update_date"
    private const val UPDATE_CHANNEL_KEY = "update_check_channel"
    private const val UPDATE_NOTICE_VERSION_KEY = "update_notice_version"

    private const val LAST_EXCEPTION_KEY = "last_exception"

    var VIP_STATE: Int get() = getInt(VIP_STATE_KEY)
        set(value) { putInt(VIP_STATE_KEY, value) }

    var VIP_TYPE: Int get() = getInt(VIP_TYPE_KEY)
        set(value) { putInt(VIP_TYPE_KEY, value) }

    var VIP_LABEL: String get() = getString(VIP_LABEL_KEY)
        set(value) { putString(VIP_LABEL_KEY, value) }

    var IS_LOGIN: Boolean get() = if (ACCESS_TOKEN == "") false
        else getBoolean(IS_LOGIN_KEY)
        set(value) { putBoolean(IS_LOGIN_KEY, value) }

    var MID: Long get() = getLong(MID_KEY)
        set(value) { putLong(MID_KEY, value) }

    var ACCESS_TOKEN: String get() = getString(ACCESS_TOKEN_KEY)
        set(value) { putString(ACCESS_TOKEN_KEY, value) }

    var REFRESH_TOKEN: String get() = getString(REFRESH_TOKEN_KEY)
        set(value) { putString(REFRESH_TOKEN_KEY, value) }

    var TOKEN_EXPIRE: Long get() = getLong(TOKEN_EXPIRE_KEY)
        set(value) { putLong(TOKEN_EXPIRE_KEY, value) }

    var NAME: String get() = getString(NAME_KEY, "哔哩番剧用户")
        set(value) { putString(NAME_KEY, value) }

    var SIGN: String get() = getString(SIGN_KEY, "这个人很懒，什么也没有留下。")
        set(value) { putString(SIGN_KEY, value) }

    var FACE: String get() = getString(FACE_KEY)
        set(value) { putString(FACE_KEY, value) }

    var SEX: Int get() = getInt(SEX_KEY)
        set(value) { putInt(SEX_KEY, value) }

    var LEVEL: Int get() = getInt(LEVEL_KEY)
        set(value) { putInt(LEVEL_KEY, value) }

    var BASE_DIR: String get() = getString(BASE_DIR_KEY, DEFAULT_DIR)
        set(value) { putString(BASE_DIR_KEY, value) }
    val DEFAULT_DIR get() = BuildConfig.APPLICATION_ID

    var PLAYER_AUTO_NEXT: Boolean get() = getBoolean(PLAYER_AUTO_NEXT_KEY, true)
        set(value) { putBoolean(PLAYER_AUTO_NEXT_KEY, value) }

    var PACKAGE: String get() {
        val value = getString(PACKAGE_KEY, DEFAULT_TYPE)
        return if (PACKAGES.keys.contains(value)) value else
            DEFAULT_TYPE
    } set(value) {
        if (PACKAGES.keys.contains(value)) {
            putString(PACKAGE_KEY, value)
        } else {
            putString(PACKAGE_KEY, DEFAULT_TYPE)
        }
    }
    val PACKAGE_NAME: String get() = PACKAGES[PACKAGE]!!

    var TASK_AUTO_START: Boolean get() = getBoolean(TASK_AUTO_START_KEY, true)
        set(value) { putBoolean(TASK_AUTO_START_KEY, value) }

    var TASK_PARALLEL_COUNT: Int get() {
        val value = getInt(TASK_PARALLEL_COUNT_KEY, DEFAULT_TASK_PARALLEL_COUNT)
        return if(value in MIN_TASK_PARALLEL_COUNT .. MAX_TASK_PARALLEL_COUNT) value else
            DEFAULT_TASK_PARALLEL_COUNT
    } set(value) {
        if(value in MIN_TASK_PARALLEL_COUNT .. MAX_TASK_PARALLEL_COUNT) {
            putInt(TASK_PARALLEL_COUNT_KEY, value)
        } else {
            putInt(TASK_PARALLEL_COUNT_KEY, DEFAULT_TASK_PARALLEL_COUNT)
        }
    }
    private const val DEFAULT_TASK_PARALLEL_COUNT = 1
    const val MAX_TASK_PARALLEL_COUNT = 3
    const val MIN_TASK_PARALLEL_COUNT = 1

    var QUALITY: Int get() {
        val value = getInt(QUALITY_KEY, DEFAULT_QUALITY)
        return if (QUALITIES.keys.contains(value)) value else
            DEFAULT_QUALITY
    } set(value) {
        if (QUALITIES.keys.contains(value)) {
            putInt(QUALITY_KEY, value)
        } else {
            putInt(QUALITY_KEY, DEFAULT_QUALITY)
        }
    }
    val QUALITY_NAME: String get() = QUALITIES[QUALITY]!!

    private val UPDATE_DATE: String get() = getString(UPDATE_DATE_KEY, Date(0).toHttpDateString())
    private val CURRENT_DATE: String get() = SimpleDateFormat("yyMMdd", Locale.CHINA).format(Date())
    fun onUpdate() { putString(UPDATE_DATE_KEY, CURRENT_DATE) }
    fun needAutoCheckUpdate() = CURRENT_DATE != UPDATE_DATE

    var LAST_EXCEPTION: String get() = getString(LAST_EXCEPTION_KEY)
    set(value) { putString(LAST_EXCEPTION_KEY, value) }

    val PACKAGES = mapOf(
        "tv.danmaku.bili" to "正式版",
        "com.bilibili.app.blue" to "概念版",
        "com.bilibili.app.in" to "国际版"
    )
    const val DEFAULT_TYPE = "tv.danmaku.bili"

    val INSTALLED_CLIENTS: Map<String, String> get() {
        val result = hashMapOf<String, String>()
        PACKAGES.map {
            try {
                Application.APPLICATION.packageManager.getPackageInfo(it.key, 0)
                result[it.key] = it.value
            } catch (e: PackageManager.NameNotFoundException) { }
        }
        return result
    }

    val CLIENT_AVAILABLE: Boolean get() = INSTALLED_CLIENTS.isNotEmpty()

    val QUALITIES = mapOf(
        125 to "HDR 真彩色",
        120 to "4K 超清",
        116 to "1080P60 高帧率",
        112 to "1080P 高码率",
        80 to "1080P 高清",
        74 to "720P60 高帧率",
        64 to "720P 高清",
        32 to "480P 清晰",
        16 to "360P 流畅"
    )
    const val DEFAULT_QUALITY = 80

    val API_SERVERS = arrayListOf(
        "repost.98e.org",
        "bilibili-tw-api.kghost.info",
        "api.qiu.moe"
    )

    val UPDATE_CHANNELS = listOf(
        BuildConfig.TYPE_RELEASE,
        BuildConfig.TYPE_DEV
    )
    var UPDATE_CHANNEL: String
        get() {
            val channel = getString(UPDATE_CHANNEL_KEY, DEFAULT_UPDATE_CHANNELS)
            return if (!UPDATE_CHANNELS.contains(channel))
                DEFAULT_UPDATE_CHANNELS else channel
        }
        set(value) {
            if (UPDATE_CHANNELS.contains(value)) {
                putString(UPDATE_CHANNEL_KEY, value)
            } else {
                putString(UPDATE_CHANNEL_KEY, DEFAULT_UPDATE_CHANNELS)
            }
        }
    val DEFAULT_UPDATE_CHANNELS = BuildConfig.TYPE_RELEASE

    @Deprecated("")
    var UPDATE_NOTICE_VERSION: Int get() = getInt(UPDATE_NOTICE_VERSION_KEY)
        set(value) { putInt(UPDATE_NOTICE_VERSION_KEY, value) }

    fun saveUserData(data: UserData) {
        NAME = data.name
        SIGN = data.sign
        FACE = data.face
        SEX = data.sex
        VIP_TYPE = data.vipType
        VIP_STATE = data.vipState
        VIP_LABEL = data.vipLabel
        LEVEL = data.level
    }

    private fun getString(key: String, defValue: String = "") =
        sharedPreferences.getString(key, defValue).toString()
    private fun getInt(key: String, defValue: Int = 0) =
        sharedPreferences.getInt(key, defValue)
    private fun getLong(key: String, defValue: Long = 0L) =
        sharedPreferences.getLong(key, defValue)
    private fun getBoolean(key: String, defValue: Boolean = false) =
        sharedPreferences.getBoolean(key, defValue)

    private fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    private fun putInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }
    private fun putLong(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }
    private fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    private val sharedPreferences: SharedPreferences get() = Application.APPLICATION_CONTEXT
        .getSharedPreferences("user", Context.MODE_PRIVATE)
        ?: throw NullPointerException("SharedPreferences 'user' not found")
}
