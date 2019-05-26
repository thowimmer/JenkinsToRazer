import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.url
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.*
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import platform.posix.sleep

class RazerClient {

    private lateinit var sessionUri: String

    @UseExperimental(UnstableDefault::class)
    private val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json.nonstrict).apply {
                setMapper(InitializationRequest::class, InitializationRequest.serializer())
                setMapper(InitializationResponse::class, InitializationResponse.serializer())
            }
        }
    }

    suspend fun init(){
        val initResponse = callRazerSdkInitialization()
        sessionUri = initResponse.uri
    }

    @InternalCoroutinesApi
    suspend fun run(){
        while (isActive){
            callHeartbeat()
            sleep(2)
        }
    }

    suspend fun setColor (color: Int) {
        callSetStaticColor(color)
    }

    private suspend fun callRazerSdkInitialization() : InitializationResponse =
            client.post{
                url("http://localhost:54235/razer/chromasdk")
                contentType(ContentType.Application.Json)
                body = InitializationRequest(
                        "JenkinsToRazer",
                        "Visualize build status of a Jenkins job on a Razer Chroma compatible keyboard",
                        InitializationRequest.Author("Thomas Wimmer", "https://github.com/thowimmer"),
                        supportedDevices = setOf("keyboard"),
                        category = "application"
                )
            }

    private suspend fun callHeartbeat() {
        val response = client.put<String>("$sessionUri/heartbeat")
        println("Heartbeat Response: $response")
    }

    private suspend fun callSetStaticColor(color : Int) {
        val response = client.put<String> {
            url("$sessionUri/keyboard")
            body = TextContent("{\"effect\":\"CHROMA_STATIC\",\"param\":{\"color\":$color}}", contentType = ContentType.Application.Json)
        }
        println("Static Color Response: $response")
    }
}

@Serializable
data class InitializationRequest(
        val title: String,
        val description: String,
        val author: Author,
        @SerialName("device_supported")
        val supportedDevices: Set<String>,
        val category: String

){
    @Serializable
    data class Author(val name: String, val contact: String)
}

@Serializable
data class InitializationResponse(
        @SerialName("sessionid")
        val sessionId: Int,
        val uri: String

)