package cat.xojan.random1.feature.downloads

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import android.widget.Toast
import cat.xojan.random1.Application
import cat.xojan.random1.R
import cat.xojan.random1.domain.model.CrashReporter
import cat.xojan.random1.domain.model.EventLogger
import cat.xojan.random1.domain.interactor.PodcastDataInteractor
import javax.inject.Inject


class DownloadCompleteReceiver : BroadcastReceiver() {

    @Inject internal lateinit var podcastDataInteractor: PodcastDataInteractor
    @Inject internal lateinit var downloadManager: DownloadManager
    @Inject internal lateinit var eventLogger: EventLogger
    @Inject internal lateinit var crashReporter: CrashReporter

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive()")
        initInjector(context)

        val reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        val query = DownloadManager.Query()
        query.setFilterById(reference)
        val cursor = downloadManager.query(query)

        if (cursor != null && cursor.moveToFirst()) {
            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val status = cursor.getInt(statusIndex)

            when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> {

                    val titleIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
                    val title = cursor.getString(titleIndex)

                    val localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                    val uri = cursor.getString(localUriIndex)
                    val audioId = uri.split((Environment.DIRECTORY_DOWNLOADS + "/").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                            .replace(PodcastDataInteractor.EXTENSION, "")

                    eventLogger.logDownloadedPodcast(title)
                    podcastDataInteractor.addDownload(audioId)

                    Toast.makeText(context, context.getString(R.string.download_successful) + ": " +
                            title, Toast.LENGTH_SHORT).show()
                }

                DownloadManager.STATUS_FAILED -> {
                    val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                    val reason = cursor.getInt(reasonIndex)
                    var reasonText: String? = null

                    when (reason) {
                        DownloadManager.ERROR_CANNOT_RESUME -> reasonText = "ERROR_CANNOT_RESUME"
                        DownloadManager.ERROR_DEVICE_NOT_FOUND -> reasonText = "ERROR_DEVICE_NOT_FOUND"
                        DownloadManager.ERROR_FILE_ALREADY_EXISTS -> reasonText = "ERROR_FILE_ALREADY_EXISTS"
                        DownloadManager.ERROR_FILE_ERROR -> reasonText = "ERROR_FILE_ERROR"
                        DownloadManager.ERROR_HTTP_DATA_ERROR -> reasonText = "ERROR_HTTP_DATA_ERROR"
                        DownloadManager.ERROR_INSUFFICIENT_SPACE -> reasonText = "ERROR_INSUFFICIENT_SPACE"
                        DownloadManager.ERROR_TOO_MANY_REDIRECTS -> reasonText = "ERROR_TOO_MANY_REDIRECTS"
                        DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> reasonText = "ERROR_UNHANDLED_HTTP_CODE"
                        DownloadManager.ERROR_UNKNOWN -> reasonText = "ERROR_UNKNOWN"
                    }

                    crashReporter.logException("Download failed: $reason $reasonText")
                    podcastDataInteractor.deleteDownloading(reference)
                    Toast.makeText(context, context.getString(R.string.download_failed) + ": "
                            + reasonText, Toast.LENGTH_SHORT).show()
                }
            }
            cursor.close()
        } else {
            podcastDataInteractor.deleteDownloading(reference)
            Toast.makeText(context, context.getString(R.string.download_cancelled),
                    Toast.LENGTH_SHORT).show()
        }
        podcastDataInteractor.refreshDownloadedPodcasts()
    }

    private fun initInjector(context: Context) {
        (context.applicationContext as Application).appComponent.inject(this)
    }

    companion object {

        private val TAG = DownloadCompleteReceiver::class.java.simpleName
    }
}