package cat.xojan.random1.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Podcast(var id: String,
                   var remoteUrl: String,
                   var filePath: String?,
                   var dateTime: Date?,
                   var durationSeconds: Long,
                   var programId: String,
                   var smallImageUrl: String?,
                   var bigImageUrl: String?,
                   var state: PodcastState,
                   var title: String,
                   var downloadReference: Long = 0
) : Parcelable {

    companion object {
        val PODCAST_STATE = "podcast_state"
        val PODCAST_FILE_PATH = "podcast_downloaded_file_path"
        val PODCAST_DOWNLOAD_REFERENCE = "podcast_download_reference"
        val PODCAST_PROGRAM_ID = "podcast_program_id"
        val PODCAST_BIG_IMAGE_URL = "podcast_big_image_url"
        val PODCAST_DURATION = "podcast_duration"
        val PODCAST_DATE = "podcast_date"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val podcast = other as Podcast

        if (remoteUrl != podcast.remoteUrl) return false
        if (programId != podcast.programId) return false
        return title == podcast.title
    }

    override fun hashCode(): Int {
        var result = remoteUrl.hashCode()
        result = 31 * result + programId.hashCode()
        result = 31 * result + title.hashCode()
        return result
    }
}