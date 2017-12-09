package cat.xojan.random1.data

import cat.xojan.random1.domain.model.Podcast

class MediaDescriptionCompatJson(
        val id: String?,
        val title: String,
        val mediaUrl: String?,
        val iconUrl: String?,
        val state: Podcast.State,
        val downloadReference: Long?,
        val mediaFilePath: String?
)