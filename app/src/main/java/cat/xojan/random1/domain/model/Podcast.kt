package cat.xojan.random1.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Podcast(private var audio: Audio,
              var path: String,
              var filePath: String?,
              var dateTime: Date?,
              private var durationSeconds: Long,
              var programId: String?,
              private var _imageUrl: String?,
              private var _state: State?,
              private var appMobileTitle: String,
              var downloadReference: Long = 0
) : Parcelable {

    companion object {
        val PODCAST_STATE = "podcast_state"
        val PODCAST_FILE_PATH = "podcast_downloaded_file_path"
        val PODCAST_DOWNLOAD_REFERENCE = "podcast_download_reference"
    }

    val title: String
        get() = appMobileTitle

    var imageUrl: String?
        get() = _imageUrl
        set(value) {
            _imageUrl = value
        }

    var state: State
        get() {
            _state?.let {
                return _state as State
            }
            return State.LOADED
        }
        set(value) {
            _state = value
        }

    @Transient
    var audioId: String? = null
        get() = audio.id

    enum class State {
        LOADED,
        DOWNLOADING,
        DOWNLOADED
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val podcast = other as Podcast

        if (path != podcast.path) return false
        if (programId != podcast.programId) return false
        return appMobileTitle == podcast.appMobileTitle
    }

    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + programId!!.hashCode()
        result = 31 * result + appMobileTitle.hashCode()
        return result
    }
}