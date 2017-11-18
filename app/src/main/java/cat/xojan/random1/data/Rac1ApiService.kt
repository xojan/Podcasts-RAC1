package cat.xojan.random1.data

import cat.xojan.random1.domain.entities.PodcastData
import cat.xojan.random1.domain.entities.ProgramData
import io.reactivex.Flowable
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface Rac1ApiService {
    @GET("v1/programs")
    fun getProgramData(): Call<ProgramData>

    @GET("v1/sessions/{programId}")
    fun getPodcastData(@Path("programId") programId: String): Flowable<PodcastData>

    @GET("v1/sessions/{programId}/{sectionId}")
    fun getPodcastData(@Path("programId") programId: String,
                       @Path("sectionId") sectionId: String): Flowable<PodcastData>

    @GET("v1/sessions/{programId}")
    fun getPodcastDataPlainData(@Path("programId") programId: String): Call<PodcastData>

    @GET("v1/sessions/{programId}/{sectionId}")
    fun getPodcastDataPlainData(@Path("programId") programId: String,
                                @Path("sectionId") sectionId: String): Call<PodcastData>
}