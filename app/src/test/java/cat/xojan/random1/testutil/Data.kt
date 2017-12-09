package cat.xojan.random1.testutil

import cat.xojan.random1.domain.model.*
import java.util.*

val podcast1 = Podcast(Audio("audioId"), "path1", "filePath1", Date(), 0, "podcastId1", null, Podcast.State.DOWNLOADED, "programTitle1", 1)
val podcast2 = Podcast(Audio("audioId"), "path1", null, Date(), 0, "podcastId2", null, null, "programTitle1", 1)
val podcast3 = Podcast(Audio("audioId"), "path1", "filePath3", Date(), 0, "podcastId3", null, Podcast.State.DOWNLOADING, "programTitle1", 1)
val podcastList = listOf(podcast1, podcast2, podcast3)

val program1 = Program("programId1", "programTitle1", listOf(), Images("http://image.url"), true)
val program2 = Program("programId2", "programTitle2", listOf(), Images("http://image.url"), false)
val program3 = Program("programId3", "programTitle3", listOf(), Images("http://image.url"), true)
val programList = listOf(program1, program2, program3)
val programListResult1 = listOf(program1, program3)

val section1 = Section("sectionId1", "sectionTitle1", "http://image.url", true, SectionType.SECTION)
val section2 = Section("sectionId2", "sectionTitle2", "http://image.url", false, SectionType.SECTION)
val section3 = Section("sectionId3", "sectionTitle3", "http://image.url", true, SectionType.GENERIC)
val section4 = Section("sectionId4", "sectionTitle4", "http://image.url", true, SectionType.SECTION)
val sectionList = listOf(section1, section2, section3, section4)
val sectionListResult1 = listOf(section1, section4)