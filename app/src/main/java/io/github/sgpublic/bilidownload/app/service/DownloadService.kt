package io.github.sgpublic.bilidownload.app.service

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import bilibili.pgc.gateway.player.v2.Playurl.Stream
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.common.HttpOption
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.base.app.BaseService
import io.github.sgpublic.bilidownload.core.exsp.BangumiPreference
import io.github.sgpublic.bilidownload.core.grpc.client.AppClient
import io.github.sgpublic.bilidownload.core.room.dao.DownloadTaskDao
import io.github.sgpublic.bilidownload.core.room.entity.DownloadTaskEntity
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

    private val Path: File by lazy {
        File(Environment.getExternalStorageDirectory(), "download")
            .also { it.mkdirs() }
    }

    override fun onChanged(tasks: List<DownloadTaskEntity>) {
        // 同时只允许一个任务
        if (tasks.isNotEmpty()) {
            return
        }
        val next = Dao.oneWaiting
        if (next == null) {
            stopSelf(0)
            return
        }
        log.info("starting task(epid: ${next.epid}, cid: ${next.cid}, qn: ${next.qn})")
        try {
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
                it["ss${next.sid}/ep${next.epid}/video.m4s"] = video.dashVideo.baseUrl
                it["ss${next.sid}/ep${next.epid}/audio.m4s"] = audio.baseUrl
            }
            "ss${next.sid}/ep${next.epid}/cover.png".let { epCover ->
                if (File(Path, epCover).exists()) {
                    return@let
                }
                urls[epCover] = next.episodeCover
            }
            "ss${next.sid}/cover.png".let { ssCover ->
                if (File(Path, ssCover).exists()) {
                    return@let
                }
                urls[ssCover] = next.seasonCover
            }
            val header = HttpOption()
                .addHeader("Referer", "https://www.bilibili.com/bangumi/play/ep${next.epid}")
            val id = Aria.download(this)
                .loadGroup(ArrayList(urls.values))
                .setDirPath(Path.canonicalPath)
                .setSubFileName(ArrayList(urls.keys))
                .option(header)
                .ignoreCheckPermissions()
                .ignoreFilePathOccupy()
                .unknownSize()
                .create()
            next.taskId = id
            next.statusMessage = ""
            next.status = DownloadTaskEntity.Status.Processing
            log.warn("Task started!")
        } catch (e: Exception) {
            log.warn("Task start failed.", e)
            next.statusMessage = e.requiredMessage()
            next.status = DownloadTaskEntity.Status.Error
        }
        Dao.save(next)
    }

    override fun onServiceCreated() {
        log.info("Start DownloadService")
        started = true
        ProcessingTasks.observeForever(this)
    }

    override fun onDestroy() {
        log.info("Destroy DownloadService")
        ProcessingTasks.removeObserver(this)
        AppClient.closeQuietly()
        started = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private var started = false
        fun startService(origin: Context) {
            if (!started) {
                return
            }
            origin.startService(Intent(origin, DownloadService::class.java))
        }
    }
}