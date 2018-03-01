package cat.xojan.random1.data

import cat.xojan.random1.domain.model.Program
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Rfc3339DateJsonAdapter
import io.reactivex.observers.TestObserver
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.apache.commons.io.IOUtils
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.SocketTimeoutException
import java.util.*


class RemoteProgramRepositoryTest {

    private lateinit var remoteProgramRepository: RemoteProgramRepository
    private lateinit var rac1ApiService: ApiService
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()

        val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                .build()

        val retrofit = Retrofit.Builder()
                .baseUrl(mockWebServer.url("").toString())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

        rac1ApiService = retrofit.create(ApiService::class.java)
        remoteProgramRepository = RemoteProgramRepository(rac1ApiService)
    }

    @Test @Ignore
    fun get_program_list_success() {
        val body = IOUtils.toString(this.javaClass.classLoader.getResourceAsStream("programs.json"))
        mockWebServer.enqueue(MockResponse().setBody(body))
        val testSubscriber = TestObserver<List<Program>>()

        remoteProgramRepository.getPrograms(true).subscribe(testSubscriber)
        testSubscriber.assertValue(
            listOf(
                Program("el-mon", null, null, null),
                Program("la-competencia", null, null, null),
                Program("la-segona-hora", null, null, null)))
    }

    /*@Test
    fun get_program_list_fail() {
        mockWebServer.enqueue(MockResponse().setResponseCode(404))
        val testSubscriber = TestObserver<List<Program>>()

        remoteProgramRepository.getPrograms().subscribe(testSubscriber)
        testSubscriber.assertErrorMessage("Client Error")
    }*/

    /*@Test @Ignore
    fun get_program() {
        remoteProgramRepository.programs = programsMap

        val program = remoteProgramRepository.getProgram("programId2")
        assertThat(program, equalTo(program2))
    }*/

    /*@Test @Ignore
    fun has_sections() {
        remoteProgramRepository.programs = programsMap

        assertThat(remoteProgramRepository.hasSections("programId1"),
                equalTo(true))
    }*/

   /* @Test @Ignore
    fun has_no_sections() {
        remoteProgramRepository.programs = programsMap

        assertThat(remoteProgramRepository.hasSections("programId2"),
                equalTo(false))
    }*/

    /*@Test
    fun get_section_list_success() {
        remoteProgramRepository.programs = programsMap
        val testSubscriber = TestObserver<List<Section>>()

        remoteProgramRepository.getSections("programId1").subscribe(testSubscriber)
        testSubscriber.assertValue(sectionList)
    }*/

    /*@Test @Ignore
    fun get_section_list_fail() {
        remoteProgramRepository.programs = programsMap
        val testSubscriber = TestObserver<List<Section>>()

        remoteProgramRepository.getSections("programId104").subscribe(testSubscriber)
        testSubscriber.assertError(InvalidKeyException::class.java)
    }*/

    @Test
    fun catches_timeout_exceptions() {
        val mockResponse = MockResponse()
        mockResponse.socketPolicy = SocketPolicy.NO_RESPONSE
        mockWebServer.enqueue(mockResponse)
        val testSubscriber = TestObserver<List<Program>>()

        remoteProgramRepository.getPrograms(true).subscribe(testSubscriber)
        testSubscriber.assertError(SocketTimeoutException::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
}

