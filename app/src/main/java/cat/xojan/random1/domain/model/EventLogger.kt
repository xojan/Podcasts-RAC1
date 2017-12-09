package cat.xojan.random1.domain.model

import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.ContentViewEvent
import com.crashlytics.android.answers.CustomEvent


class EventLogger(val answers: Answers?) {

    fun logDownloadedPodcast(title: String) {
        answers?.logCustom(CustomEvent("Downloads").putCustomAttribute("title", title))

    }

    fun logPlayedPodcast(podcast: Podcast) {
        answers?.logContentView(ContentViewEvent().putContentName(podcast.title))
    }

    fun logExportedPodcast(podcastTitle: String) {
        answers?.logCustom(CustomEvent("Exported").putCustomAttribute("title", podcastTitle))
    }

    fun logExportedPodcastAction() {
        answers?.logCustom(CustomEvent("ExportAction"))
    }
}