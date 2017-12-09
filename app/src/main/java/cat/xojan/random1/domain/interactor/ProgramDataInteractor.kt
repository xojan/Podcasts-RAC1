package cat.xojan.random1.domain.interactor
import android.content.Context
import android.os.Environment
import android.text.TextUtils
import cat.xojan.random1.data.SharedPrefDownloadPodcastRepository
import cat.xojan.random1.domain.model.*
import cat.xojan.random1.domain.repository.ProgramRepository
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

class ProgramDataInteractor @Inject constructor(
        private val programRepo: ProgramRepository,
        private val downloadRepo: SharedPrefDownloadPodcastRepository,
        private val context: Context,
        private val eventLogger: EventLogger) {

    fun loadPrograms(): Single<List<Program>> {
        return programRepo.getPrograms()
                .flatMap {
                    podcasts -> Observable.just(podcasts)
                        .flatMapIterable { p -> p }
                        .filter { p -> p.active }
                        .toList()
                }
    }

    fun hasSections(programId: String): Boolean {
        return programRepo.hasSections(programId)
    }

    fun loadSections(programId: String): Single<List<Section>> {
        val program = programRepo.getProgram(programId)
        return programRepo.getSections(programId)
                .flatMap {
                    sections -> Observable.just(sections)
                        .flatMapIterable { s -> s }
                        .filter { s -> s.active }
                        .filter { s -> s.type == SectionType.SECTION }
                        .map { s ->
                            program?.let {
                                s.imageUrl = program.imageUrl()
                                s.programId = program.id
                            }
                            s
                        }
                        .toList()
                }
    }

    fun getDownloadedPodcastTitle(audioId: String): String? {
        return downloadRepo.getDownloadedPodcastTitle(audioId)
    }

    fun exportPodcasts(): Observable<Boolean> {
        eventLogger.logExportedPodcastAction()
        return Observable.create { e ->
            val iternalFileDir = context.getExternalFilesDir(Environment.DIRECTORY_PODCASTS)
            val externalFilesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PODCASTS)

            externalFilesDir.mkdirs()

            for (podcastFile in iternalFileDir!!.listFiles()) {
                val audioId = podcastFile.getPath()
                        .split((Environment.DIRECTORY_PODCASTS + "/").toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[1].replace(".mp3", "")
                var podcastTitle = getDownloadedPodcastTitle(audioId)

                if (!TextUtils.isEmpty(podcastTitle)) {
                    podcastTitle = podcastTitle!!.replace("/", "-")
                    val dest = File(externalFilesDir, podcastTitle + ".mp3")
                    copy(podcastFile, dest)
                    eventLogger.logExportedPodcast(podcastTitle)
                }
            }
            e.onNext(true)
        }
    }

    private fun copy(src: File, dst: File) {
        try {
            val inStream = FileInputStream(src)
            val outStream = FileOutputStream(dst)
            val inChannel = inStream.channel
            val outChannel = outStream.channel
            inChannel.transferTo(0, inChannel.size(), outChannel)
            inStream.close()
            outStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}