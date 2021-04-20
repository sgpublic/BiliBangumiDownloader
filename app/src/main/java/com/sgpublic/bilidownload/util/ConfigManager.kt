package com.sgpublic.bilidownload.util

import android.content.Context
import android.content.SharedPreferences

class ConfigManager(val context: Context) {
    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun getString(key: String, defValue: String = "") = sharedPreferences.getString(key, defValue).toString()
    fun getInt(key: String, defValue: Int = 0) = sharedPreferences.getInt(key, defValue)
    fun getLong(key: String, defValue: Long = 0L) = sharedPreferences.getLong(key, defValue)
    fun getBoolean(key: String, defValue: Boolean = false) = sharedPreferences.getBoolean(key, defValue)

    fun putString(key: String, value: String): ConfigManager{
        editor.putString(key, value)
        return this
    }
    fun putInt(key: String, value: Int): ConfigManager{
        editor.putInt(key, value)
        return this
    }
    fun putLong(key: String, value: Long): ConfigManager{
        editor.putLong(key, value)
        return this
    }
    fun putBoolean(key: String, value: Boolean): ConfigManager{
        editor.putBoolean(key, value)
        return this
    }
    fun apply(){
        editor.apply()
    }
}