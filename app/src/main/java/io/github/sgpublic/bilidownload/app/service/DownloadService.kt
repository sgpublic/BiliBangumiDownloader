package io.github.sgpublic.bilidownload.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Environment
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import bilibili.pgc.gateway.player.v2.Playurl.Stream
import com.arialyy.annotations.Download
import com.arialyy.annotations.DownloadGroup
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.common.HttpOption
import com.arialyy.aria.core.download.DownloadGroupEntity
import com.arialyy.aria.core.inf.IEntity
import com.arialyy.aria.core.listener.ISchedulers
import com.arialyy.aria.core.task.DownloadGroupTask
import com.arialyy.aria.exception.AriaGroupException
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.BuildConfig
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.app.notifacation.NotifyChannel
import io.github.sgpublic.bilidownload.base.app.BaseService
import io.github.sgpublic.bilidownload.core.exsp.BangumiPreference
import io.github.sgpublic.bilidownload.core.forest.core.UrlEncodedInterceptor
import io.github.sgpublic.bilidownload.core.grpc.client.AppClient
import io.github.sgpublic.bilidownload.core.room.dao.DownloadTaskDao
import io.github.sgpublic.bilidownload.core.room.entity.DownloadTaskEntity
import io.github.sgpublic.bilidownload.core.util.customs
import io.github.sgpublic.bilidownload.core.util.log
import io.github.sgpublic.bilidownload.core.util.requiredMessage
import io.github.sgpublic.exsp.ExPreference
import okhttp3.internal.closeQuietly
import java.io.File
import java.util.*

/**
 *
 * @author Madray Haven
 * @date 2022/11/7 15:10
 */
class DownloadService: BaseService(), Observer<List<DownloadTaskEntity>> {
    private val Dao: DownloadTaskDao by lazy {
        Application.Database.DownloadTaskDao()
    }
    private val ProcessingTasks: LiveData<List<DownloadTaskEntity>> by lazy {
        Dao.observeProcessing()
    }
    private val AppClient: AppClient by lazy { AppClient() }

    private val Quality: Int get() = ExPreference.get<BangumiPreference>().quality

    private val ExternalDownload: File by lazy { getExternalFilesDir("Download")!! }

    override fun onChanged(tasks: List<DownloadTaskEntity>) {
        // 同时只允许一个任务
        if (tasks.isNotEmpty()) {
            log.debug("Task queue is not empty, waiting...")
            return
        }
        val next = Dao.oneWaiting
        if (next == null) {
            log.info("No more waiting tasks.")
            stopSelf(0)
            return
        }
        log.info("starting task(epid: ${next.epid}, cid: ${next.cid}, qn: ${next.qn})")
        try {
            if (next.taskId > 0) {
                val task = Aria.download(this)
                    .loadGroup(next.taskId)
                    .customs()
                when (task.entity.state) {
                    IEntity.STATE_FAIL -> {
                        next.status = DownloadTaskEntity.Status.Error
                        log.info("Task failed! task_id: ${task.entity.id}")
                    }
                    IEntity.STATE_COMPLETE -> {
                        next.status = DownloadTaskEntity.Status.Finished
                        log.info("Task finished, task_id: ${task.entity.id}")
                    }
                    IEntity.STATE_RUNNING, IEntity.STATE_CANCEL -> { }
                    else -> {
                        task.resume()
                        next.status = DownloadTaskEntity.Status.Processing
                        log.info("Task resumed, task_id: ${next.taskId}")
                    }
                }
                return
            }
            val execute = AppClient.getPlayUrl(next.cid, next.epid, next.qn).execute()
            val fitted = PriorityQueue<Stream> { o1, o2 -> o2.info.quality - o1.info.quality }
            var min: Stream? = null
            for (stream in execute.videoInfo.streamListList) {
                if (stream.info.quality <= Quality) {
                    fitted.add(stream)
                }
                if (min == null || min.info.quality > stream.info.quality) {
                    min = stream
                }
            }
            val video = fitted.peek().also { fitted.clear() } ?: min!!
            next.qn = video.info.quality
            val audio = execute.videoInfo.dashAudioList.find {
                it.id == video.dashVideo.audioId
            } ?: throw Exception("找不到合适的清晰度")
            val urls = LinkedHashMap<String, String>().also {
                it["video.m4s"] = video.dashVideo.baseUrl
                it["audio.m4s"] = audio.baseUrl
            }
            val id = Aria.download(this)
                .loadGroup(ArrayList(urls.values))
                .setDirPath(File(ExternalDownload, "ss${next.sid}/ep${next.epid}/").canonicalPath)
                .setSubFileName(ArrayList(urls.keys))
                .customs()
                .create()
            next.taskId = id
            next.statusMessage = ""
            next.status = DownloadTaskEntity.Status.Processing
            log.info("Task started!\n" +
                    "  video: ${video.dashVideo.baseUrl}\n" +
                    "  audio: ${audio.baseUrl}")
        } catch (e: Exception) {
            log.warn("Task start failed.", e)
            next.statusMessage = e.requiredMessage()
            next.status = DownloadTaskEntity.Status.Error
        } finally {
            Dao.set(next)
        }
    }

    override fun onCreate() {
        super.onCreate()
        log.info("DownloadService onCreate")
        started = true
        ProcessingTasks.observeForever(this)
        NotifyChannel.DownloadingTask.newBuilder(this)
            .setContentTitle(getString(R.string.title_download_service))
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
            .startForeground(1)
        Aria.download(this).register()
    }

    override fun onDestroy() {
        super.onDestroy()
        log.info("DownloadService onDestroy")
        ProcessingTasks.removeObserver(this)
        AppClient.closeQuietly()
        Aria.download(this).unRegister()
        started = false
    }

    @DownloadGroup.onTaskComplete
    fun onTaskComplete(task: DownloadGroupTask) {
        log.info("Task finished, task_id: ${task.entity.id}")
        val entity = Dao.getByTaskId(task.entity.id)
        entity.status = DownloadTaskEntity.Status.Finished
        Dao.set(entity)
    }
    @DownloadGroup.onTaskFail
    fun onTaskFail(task: DownloadGroupTask, e: AriaGroupException) {
        log.warn("Task failed! task_id: ${task.entity.id}")
        val entity = Dao.getByTaskId(task.entity.id)
        entity.status = DownloadTaskEntity.Status.Error
        entity.statusMessage = e.requiredMessage()
        Dao.set(entity)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private var started = false
        fun startService(origin: Context) {
            if (started) {
                log.warn("Download service is already started!")
            } else {
                origin.startForegroundService(Intent(origin, DownloadService::class.java))
            }
        }
    }
}