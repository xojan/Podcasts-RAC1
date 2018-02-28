package cat.xojan.random1.domain.model

data class PodcastJson(
        val appMobileTitle: String,
        val id: String,
        val dateTime: String,
        val durationSeconds: Long,
        val mFilePath: String?,
        val mImageUrl: String?,
        val mProgramId: String,
        val mState: PodcastState,
        val path: String
)