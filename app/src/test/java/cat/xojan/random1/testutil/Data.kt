package cat.xojan.random1.testutil

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import cat.xojan.random1.domain.model.Podcast
import cat.xojan.random1.domain.model.PodcastState
import cat.xojan.random1.domain.model.Program
import cat.xojan.random1.domain.model.Section
import java.util.*

val podcast1 = Podcast("id1", "path1", "filePath1", Date(), 0, "podcastId1",
        null, null, PodcastState.DOWNLOADED, "programTitle1", 1)
val podcast2 = Podcast("id2", "path1", null, Date(), 0, "podcastId2", null,
        null, PodcastState.LOADED, "programTitle1", 1)
val podcast3 = Podcast("id3", "path1", "filePath3", Date(), 0, "podcastId3",
        null, null, PodcastState.DOWNLOADING, "programTitle1", 1)
val podcastList = listOf(podcast1, podcast2, podcast3)

val section1 = Section("sectionId1", "sectionTitle1", "http://image.url", "programId1")
val section2 = Section("sectionId2", "sectionTitle2", "http://image.url", "programId1")
val section3 = Section("sectionId3", "sectionTitle3", "http://image.url", "programId1")
val section4 = Section("sectionId4", "sectionTitle4", "http://image.url", "programId1")
val sectionList = listOf(section1, section2, section3, section4)
val sectionListResult1 = listOf(section1, section4)

// PROGRAMS
val program1 = Program("programId1", "programTitle1", null, null)
val program2 = Program("programId2", "programTitle2", null, null)
val program3 = Program("programId3", "programTitle3", null, null)
val programsMap = linkedMapOf(Pair("programId1", program1), Pair("programId2", program2), Pair("programId3", program3))

// Description item
val description1:MediaDescriptionCompat = MediaDescriptionCompat.Builder()
        .build()
val description2:MediaDescriptionCompat = MediaDescriptionCompat.Builder()
        .build()
val description3:MediaDescriptionCompat = MediaDescriptionCompat.Builder()
        .build()

// Queue item
val queueItem1 = MediaSessionCompat.QueueItem(description1, 1)
val queueItem2 = MediaSessionCompat.QueueItem(description2, 2)
val queueItem3 = MediaSessionCompat.QueueItem(description3, 3)
val queueList1Item = listOf(queueItem1)
val queueItemList = listOf(queueItem1, queueItem2, queueItem3)