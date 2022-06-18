package io.github.sgpublic.bilidownload.core.util

import com.arialyy.aria.core.download.DownloadGroupTaskListener
import com.arialyy.aria.core.task.DownloadGroupTask

interface AriaGroupDownloadListener: DownloadGroupTaskListener {
    override fun onWait(task: DownloadGroupTask) {}

    override fun onPre(task: DownloadGroupTask) {}

    override fun onTaskPre(task: DownloadGroupTask) {}

    override fun onTaskResume(task: DownloadGroupTask) {}

    override fun onTaskStart(task: DownloadGroupTask) {}

    override fun onTaskStop(task: DownloadGroupTask) {}

    override fun onTaskCancel(task: DownloadGroupTask) {}

    override fun onTaskFail(task: DownloadGroupTask, e: Exception) {}

    override fun onTaskComplete(task: DownloadGroupTask) {}

    override fun onTaskRunning(task: DownloadGroupTask) {}

    override fun onNoSupportBreakPoint(task: DownloadGroupTask) {}
}