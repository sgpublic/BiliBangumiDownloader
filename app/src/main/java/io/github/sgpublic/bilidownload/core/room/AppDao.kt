package io.github.sgpublic.bilidownload.core.room

import io.github.sgpublic.bilidownload.core.room.dao.DownloadTaskDao

/**
 *
 * @author Madray Haven
 * @date 2022/11/8 11:55
 */
interface AppDao {
    fun DownloadTaskDao(): DownloadTaskDao

    companion object {
        class Impl(private val origin: AppDatabase): AppDao {
            private val DownloadTaskDao: DownloadTaskDao by lazy { origin.DownloadTaskDao() }
            override fun DownloadTaskDao() = DownloadTaskDao
        }

        @JvmStatic
        fun of(origin: AppDatabase): AppDao {
            return Impl(origin)
        }
    }
}