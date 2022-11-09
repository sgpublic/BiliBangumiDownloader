package io.github.sgpublic.bilidownload.app.service

import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import bilibili.pgc.gateway.player.v2.Playurl.Stream
import com.arialyy.annotations.DownloadGroup
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.inf.IEntity
import com.arialyy.aria.core.task.DownloadGroupTask
import com.arialyy.aria.exception.AriaGroupException
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.app.notifacation.NotifyChannel
import io.github.sgpublic.bilidownload.base.app.BaseService
import io.github.sgpublic.bilidownload.core.exsp.BangumiPreference
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
    private val ExternalCover: File by lazy { getExternalFilesDir("Cover")!! }

    override fun onChanged(tasks: List<DownloadTaskEntity>) {
        // 同时只允许一个任务
        if (tasks.isNotEmpty()) {
            log.debug("Task queue is not empty, waiting...")
            return
        }
        val next = Dao.oneWaiting
        if (next == null) {
            log.info("No more waiting tasks, stop service.")
            stopForeground(STOP_FOREGROUND_REMOVE)
            return
        }
        log.info("starting task(epid: ${next.epid}, cid: ${next.cid}, qn: ${next.qn})")
        try {
            if (next.taskId > 0 && next.status != DownloadTaskEntity.Status.Retry) {
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
            next.taskId = newTask(next).also {
                if (it < 0) {
                    throw AriaGroupException("Task create error!")
                }
            }
            next.statusMessage = ""
            next.status = DownloadTaskEntity.Status.Processing
            log.info("Task started! task_id: ${next.taskId}")
        } catch (e: Exception) {
            log.warn("Task start failed.", e)
            next.statusMessage = e.requiredMessage()
            next.status = DownloadTaskEntity.Status.Error
        } finally {
            Dao.set(next)
        }
    }

    private fun newTask(task: DownloadTaskEntity): Long {
        val execute = AppClient.getPlayUrl(task.cid, task.epid, task.qn).execute()
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
        task.qn = video.info.quality
        val audio = execute.videoInfo.dashAudioList.find {
            it.id == video.dashVideo.audioId
        } ?: throw Exception("找不到合适的清晰度")

        val ssCover = File(ExternalCover, "s_${task.sid}")
        File(ssCover, "cover.png").takeIf { !it.exists() }?.let {
            it.parentFile?.mkdirs()
            Aria.download(this)
                .load(task.seasonCover)
                .setFilePath(it.canonicalPath)
                .customs()
                .create()
        }
        val epCover = File(ssCover, "ep${task.epid}")
        File(epCover, "cover.png").takeIf { !it.exists() }?.let {
            it.parentFile?.mkdirs()
            Aria.download(this)
                .load(task.episodeCover)
                .setFilePath(it.canonicalPath)
                .customs()
                .create()
        }

        val urls = LinkedHashMap<String, String>().also {
            it["video.m4s"] = video.dashVideo.baseUrl
            it["audio.m4s"] = audio.baseUrl
        }
        val id = File(ExternalDownload, "s_${task.sid}/ep${task.epid}/").let {
            it.mkdirs()
            Aria.download(this)
                .loadGroup(ArrayList(urls.values))
                .setDirPath(it.canonicalPath)
                .setSubFileName(ArrayList(urls.keys))
                .customs()
                .create()
        }
        log.info("Task created!\n" +
                "  video: ${video.dashVideo.baseUrl}\n" +
                "  audio: ${audio.baseUrl}")
        return id
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
            .startForeground(ID_SERVICE)
        Aria.download(this).register()
    }

    override fun onDestroy() {
        super.onDestroy()
        log.info("DownloadService onDestroy")
        ProcessingTasks.removeObserver(this)
        AppClient.closeQuietly()
        notifyIds.clear()
        Aria.download(this).unRegister()
        started = false
    }

    private val notifyIds: HashMap<Long, Int> = HashMap()
    @DownloadGroup.onTaskComplete
    fun onTaskComplete(task: DownloadGroupTask?) {
        val taskId = (task ?: return).entity.id
        log.info("Task finished, task_id: $taskId")
        val entity = Dao.getByTaskId(taskId)
        entity.status = DownloadTaskEntity.Status.Finished
        Dao.set(entity)

        NotifyChannel.DownloadInfo.newBuilder(this)
            .setContentTitle(getString(R.string.title_download_service_finish, entity.episodeTitle))
            .setContentText(entity.seasonTitle)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setGroup(GROUP_TASK_INFO)
            .setOngoing(false)
            .build()
            .send(getNotifyId(taskId))
    }
    @DownloadGroup.onTaskFail
    fun onTaskFail(task: DownloadGroupTask?, e: Exception?) {
        if (task == null) {
            log.warn("Task failed! task_id: Unknown", e)
            return
        }
        val taskId = task.entity.id
        log.warn("Task failed! task_id: $taskId", e)
        val entity = Dao.getByTaskId(taskId)
        entity.status = DownloadTaskEntity.Status.Error
        entity.statusMessage = e.requiredMessage()
        Dao.set(entity)

        NotifyChannel.DownloadInfo.newBuilder(this)
            .setContentTitle(getString(R.string.title_download_service_error, entity.episodeTitle))
            .setContentText(entity.seasonTitle)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(false)
            .build()
            .send(getNotifyId(taskId))
    }
    @DownloadGroup.onTaskRunning
    fun onTaskRunning(task: DownloadGroupTask?) {
        val taskId = (task ?: return).entity.id
        log.warn("Task running, task_id: $taskId")
        val entity = Dao.getByTaskId(taskId)

        NotifyChannel.DownloadInfo.newBuilder(this)
            .setContentTitle(getString(R.string.title_download_service_running, entity.episodeTitle))
            .setContentText(entity.seasonTitle)
            .setProgress(100, task.percent, false)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
            .send(getNotifyId(taskId))
    }

    private fun getNotifyId(taskId: Long): Int {
        if (!notifyIds.contains(taskId)) {
            notifyIds[taskId] = notifyIds.size + 2
        }
        return notifyIds[taskId]!!
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val ID_SERVICE = 1
        private const val GROUP_TASK_INFO = "GROUP_TASK_INFO"

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