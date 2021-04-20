package com.sgpublic.bilidownload.base

//import com.umeng.analytics.MobclickAgent
//import com.umeng.commonsdk.UMConfigure
//import com.umeng.message.IUmengRegisterCallback
//import com.umeng.message.PushAgent
//import org.android.agoo.mezu.MeizuRegister
//import org.android.agoo.xiaomi.MiPushRegistar
import android.app.Application
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.style.MIUIStyle
import com.sgpublic.bilidownload.util.MyLog

@Suppress("unused")
class Main : Application() {
    override fun onCreate() {
        super.onCreate()
        MyLog.v("APP启动")
        DialogX.init(this)
        DialogX.globalStyle = MIUIStyle.style()
    }
}