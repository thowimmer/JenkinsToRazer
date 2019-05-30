import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.coroutines.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json

private val MAX_ROWS = 6
private val MAX_COLUMNS = 22
private val KEY_BITMASK =  0x01000000
private val LOOP_INITERVAL_MILLIS = 50L
private val HEARBEAT_INTERVAL_MILLIS = 1000L

class RazerClient {

    private lateinit var sessionUri: String

    private val currentEffect = CustomKeyKeyBoardEffectRequest.CustomKeyBoardEffectParams()

    @UseExperimental(UnstableDefault::class)
    private val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json.nonstrict).apply {
                setMapper(InitializationRequest::class, InitializationRequest.serializer())
                setMapper(InitializationResponse::class, InitializationResponse.serializer())
                setMapper(CustomKeyKeyBoardEffectRequest::class, CustomKeyKeyBoardEffectRequest.serializer())
            }
        }
    }

    suspend fun init(){
        val initResponse = callRazerSdkInitialization()
        println(initResponse)
        sessionUri = initResponse.uri
    }

    @InternalCoroutinesApi
    suspend fun run() = coroutineScope{
        var ticksWithoutHeartbeat = 0
        while (isActive){
            if(ticksWithoutHeartbeat * LOOP_INITERVAL_MILLIS >= HEARBEAT_INTERVAL_MILLIS){
                launch { callHeartbeatEndpoint() }
                ticksWithoutHeartbeat = 0
            }else{
                ticksWithoutHeartbeat++
            }
            callCustomEffectEndpoint()
            delay(LOOP_INITERVAL_MILLIS)
        }
    }

    fun setKey(colorHex: String, rowIndex: Int, columnIndex: Int){
        currentEffect.key[rowIndex][columnIndex] = colorHex.toBgr() or KEY_BITMASK
    }

    fun setBackgroundColor (colorHex: String) {
        currentEffect.color = Array(MAX_ROWS) {Array(MAX_COLUMNS) {colorHex.toBgr()} }
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

    private suspend fun callHeartbeatEndpoint() {
        client.call("$sessionUri/heartbeat"){
            method = HttpMethod.Put
        }
    }

    private suspend fun callCustomEffectEndpoint(){
        client.call("$sessionUri/keyboard"){
            method = HttpMethod.Put
            contentType(ContentType.Application.Json)
            body = CustomKeyKeyBoardEffectRequest(param = currentEffect)
        }
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

@Serializable
data class CustomKeyKeyBoardEffectRequest(
        val effect : String = "CHROMA_CUSTOM_KEY",
        val param: CustomKeyBoardEffectParams
){
    @Serializable
    data class CustomKeyBoardEffectParams(
            var color: Array<Array<Int>> = Array(MAX_ROWS) {Array(MAX_COLUMNS) {0} },
            var key: Array<Array<Int>> = Array(MAX_ROWS) {Array(MAX_COLUMNS) {0} }
    )
}

private fun String.toBgr() : Int {
    val rgb = this.toInt(radix = 16)
    val r = (rgb and 0xFF0000)
    val g = (rgb and 0x00FF00)
    val b = (rgb and 0x0000FF)
    return (b shl 16) or g or (r shr 16)
}