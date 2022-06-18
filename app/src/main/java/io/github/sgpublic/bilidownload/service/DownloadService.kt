package io.github.sgpublic.bilidownload.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.common.HttpOption
import com.arialyy.aria.core.inf.IEntity
import com.arialyy.aria.core.task.DownloadGroupTask
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.activity.Welcome
import io.github.sgpublic.bilidownload.base.CrashHandler
import io.github.sgpublic.bilidownload.core.data.parcelable.DashIndexJson
import io.github.sgpublic.bilidownload.core.data.parcelable.EntryJson
import io.github.sgpublic.bilidownload.core.manager.ConfigManager
import io.github.sgpublic.bilidownload.core.module.ApiModule
import io.github.sgpublic.bilidownload.core.module.PlayModule
import io.github.sgpublic.bilidownload.core.util.AriaGroupDownloadListener
import io.github.sgpublic.bilidownload.core.util.LogCat
import io.github.sgpublic.bilidownload.room.entity.TaskEntity


class DownloadService: Service(), AriaGroupDownloadListener {
    private val dao = Application.DATABASE.TasksDao()
    private val option = HttpOption()
        .addHeader("User-Agent", "Bilibili Freedoooooom/Markll")

    override fun onCreate() {
        startForeground()
        listenTaskQueue()
        Aria.download(this).register()
    }

    private fun startForeground() {
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, Application.getString(R.string.app_service),
            NotificationManager.IMPORTANCE_NONE)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
        val intent = Intent(this, Welcome::class.java)
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setLargeIcon(BitmapFactory.decodeResource(applicationContext.resources,
                R.mipmap.ic_launcher))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE))
            .setContentTitle(Application.getString(R.string.app_service))
            .setWhen(ApiModule.TS)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private lateinit var processingTasks: LiveData<List<TaskEntity>>
    private val processingTasksObserver = Observer<List<TaskEntity>> { processing ->
        val waitingTasks = dao.getByTaskStatus(TaskEntity.STATUS_WAITING)
        if (processing.isNotEmpty() || waitingTasks.isEmpty()) {
            return@Observer
        }
        val data = waitingTasks[0]
        if (data.status != TaskEntity.STATUS_PREPARING) {
            dao.updateStatusByCid(data.cid, TaskEntity.STATUS_PREPARING)
        }
        val task = Aria.download(this)
            .loadGroup(data.taskId)
        if (task.taskExists() && task.taskState != IEntity.STATE_FAIL) {
            LogCat.i("恢复下载：\n" +
                    "cid: ${data.entry.source.cid}\n" +
                    "path: ${data.getBaseDir()}")
            task.unknownSize()
                .ignoreTaskOccupy()
                .ignoreCheckPermissions()
                .option(option)
                .resume()
        } else if (task.taskState == IEntity.STATE_FAIL) {
            task.cancel()
            val module = PlayModule(ConfigManager.ACCESS_TOKEN, data.entry)
            module.getPlayUrl(object : PlayModule.Callback {
                override fun onFailure(code: Int, message: String?, e: Throwable?) {
                    dao.setErrorMessageByCid(data.cid, code, message ?: "未知错误")
                }
                override fun onResolvePlayData(entry: EntryJson, index: DashIndexJson) {
                    download(data, entry, index)
                }
            }, data.entry.video_quality, false)
        }
    }

    private fun listenTaskQueue() {
        processingTasks = dao.listenByTaskStatus(TaskEntity.STATUS_PROCESSING).also {
            it.observeForever(processingTasksObserver)
        }
    }

    private fun download(data: TaskEntity, entry: EntryJson, index: DashIndexJson) {
        try {
            data.entry = entry.also {
                it.time_create_stamp = ApiModule.TS
            }
            data.index = index
            val urls = mutableMapOf(
                "video.m4s" to index.video.base_url,
                "audio.m4s" to index.audio.base_url
            )
            for (i in 0 until index.subtitles.size) {
                val subtitle = index.subtitles[i]
                urls["${subtitle.lan}.json"] = subtitle.subtitle_url
            }
            LogCat.i("开始下载：\n" +
                    "cid: ${entry.source.cid}\n" +
                    "path: ${data.getBaseDir()}")
            val taskId = Aria.download(this)
                .loadGroup(urls.values.toList())
                .setDirPath(data.getTagDir())
                .setSubFileName(urls.keys.toList())
                .unknownSize()
                .ignoreCheckPermissions()
                .ignoreFilePathOccupy()
                .option(option)
                .create()
            data.taskId = taskId
            data.save()
        } catch (e: Exception) {
            CrashHandler.saveExplosion(e, -1002, e.message ?: "未知错误")
            dao.setErrorMessageByCid(entry.source.cid, -1002, e.message ?: "未知错误")
        }
    }

    override fun onTaskFail(task: DownloadGroupTask, e: Exception) {
        CrashHandler.saveExplosion(e, -1003, e.message ?: "未知错误")
        dao.setErrorMessageByCid(getCidByTaskId(task) ?: return,
            -1003, e.message ?: "未知错误")
    }

    override fun onTaskStart(task: DownloadGroupTask) {
        LogCat.d("onTaskStart: ${task.entity?.id}")
        dao.updateStatusByCid(getCidByTaskId(task) ?: return,
            TaskEntity.STATUS_PROCESSING)
    }

    override fun onTaskResume(task: DownloadGroupTask) {
        dao.updateStatusByCid(getCidByTaskId(task) ?: return,
            TaskEntity.STATUS_PROCESSING)
        LogCat.d("onTaskResume: ${task.entity?.id}")
    }

    override fun onTaskRunning(task: DownloadGroupTask) {
        val entity = dao.getByTaskId(task.entity.id) ?: return
        entity.status = TaskEntity.STATUS_PROCESSING
        entity.entry.downloaded_bytes = task.currentProgress
        entity.entry.time_update_stamp = ApiModule.TS
        entity.save()
    }

    override fun onTaskStop(task: DownloadGroupTask) {
        dao.updateStatusByCid(getCidByTaskId(task) ?: return,
            TaskEntity.STATUS_PAUSED)
        LogCat.d("onTaskStop: ${task.entity?.id}")
    }

    override fun onTaskComplete(task: DownloadGroupTask) {
        dao.updateStatusByCid(getCidByTaskId(task) ?: return,
            TaskEntity.STATUS_FINISHED)
        LogCat.d("onTaskComplete: ${task.entity?.id}")
        if (dao.getByTaskStatus(TaskEntity.STATUS_WAITING).isEmpty()) {
            stopSelf()
        }
    }

    private fun getCidByTaskId(task: DownloadGroupTask): Long? {
        return dao.getByTaskId(task.entity.id)?.cid
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        processingTasks.removeObserver(processingTasksObserver)
        stopForeground(true)
        Aria.download(this).unRegister()
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "bilidl_download_service"
        const val NOTIFICATION_ID = 1
    }
}