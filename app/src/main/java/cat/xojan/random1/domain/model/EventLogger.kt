package cat.xojan.random1.domain.model

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.text.format.DateUtils
import cat.xojan.random1.feature.mediaplayback.QueueManager.Companion.METADATA_PROGRAM_ID
import com.google.firebase.analytics.FirebaseAnalytics

class EventLogger(val firebaseAnalytics: FirebaseAnalytics?) {

    fun logDownloadPodcastTry(audioId: String?, title: String, programId: String?) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, audioId)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, title)
        bundle.putString("item_category_id", programId)
        firebaseAnalytics?.logEvent("podcast_download_action", bundle)
    }

    fun logDownloadedPodcastSuccess(audioId: String, title: String, programTitle: String?) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, audioId)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, title)
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, programTitle)
        firebaseAnalytics?.logEvent("podcast_download_success", bundle)
    }

    fun logDownloadedPodcastFail(reason: Int, reasonText: String?) {
        val bundle = Bundle()
        bundle.putString("error_id", reason.toString())
        bundle.putString("error_msg", reasonText)
        firebaseAnalytics?.logEvent("podcast_download_fail", bundle)
    }

    fun logDownloadedPodcastCancel() {
        firebaseAnalytics?.logEvent("podcast_download_cancel", null)
    }

    fun logExportPodcastsAction() {
        firebaseAnalytics?.logEvent("podcast_export_action", null)
    }

    fun logExportedPodcast(audioId: String, podcastTitle: String?) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, audioId)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, podcastTitle)
        firebaseAnalytics?.logEvent("podcast_exported", bundle)
    }

    fun logExportedPodcastsSuccess() {
        firebaseAnalytics?.logEvent("podcast_export_success", null)
    }

    fun logExportedPodcastsFail() {
        firebaseAnalytics?.logEvent("podcast_export_fail", null)
    }

    fun logPlayedPodcast(item: MediaMetadataCompat?) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, item?.description?.mediaId)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, item?.description?.title.toString())
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, item?.getString(METADATA_PROGRAM_ID))
        firebaseAnalytics?.logEvent("podcast_played", bundle)
    }

    fun logPlayAllPodcasts() {
        firebaseAnalytics?.logEvent("podcast_play_all", null)
    }

    fun logPlaySinglePodcast() {
        firebaseAnalytics?.logEvent("podcast_play_single", null)
    }

    fun logSleepTimerAction(milliseconds: Long) {
        val bundle = Bundle()
        bundle.putString("time", DateUtils.formatElapsedTime(((milliseconds / 1000))))
        firebaseAnalytics?.logEvent("sleep_timer_action", bundle)
    }

    fun logClientPackageName(clientPackageName: String) {
        val bundle = Bundle()
        bundle.putString("client_package_name", clientPackageName)
        firebaseAnalytics?.logEvent("on_get_root", bundle)
    }

    fun logPlayerException(message: String?) {
        val bundle = Bundle()
        bundle.putString("exception_msg", message)
        firebaseAnalytics?.logEvent("player_exception", bundle)
    }
}