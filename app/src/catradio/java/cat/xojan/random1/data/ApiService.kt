package cat.xojan.random1.data

import cat.xojan.random1.domain.model.PodcastData
import cat.xojan.random1.domain.model.ProgramData
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    //http://api.ccma.cat/programesradio?en_emissio=true&amb_medias=true&agrupar=true&fills=canal&origen=llistat&canal=PUC_CR&cache=1800
    @GET("programesradio")
    fun getProgramData(
            @Query("en_emissio") inEmission: Boolean,
            @Query("agrupar") groupByLetter: Boolean,
            @Query("frontal") frontal: String, //pucr-alacarta-programa,
            @Query("origen") origin: String, //listat, frontal
            @Query("version") version: String //1.0
    ): Single<ProgramData>

    @GET("audios")
    fun getPodcastData(
            @Query("programaradio_id") programId: String
    ): Single<PodcastData>

    @GET("v1/sessions/{programId}/{sectionId}")
    fun getPodcastData(@Path("programId") programId: String,
                       @Path("sectionId") sectionId: String): Single<PodcastData>

    //https://api.ccma.cat/audios?programaradio_id=1566&ordre=-data_publicacio
    //audios.catradio.cat/multimedia/mp3/8/1/1519806320718.mp3
}