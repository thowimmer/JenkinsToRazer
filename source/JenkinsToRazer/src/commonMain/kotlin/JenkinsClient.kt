import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.Url
import io.ktor.util.InternalAPI
import io.ktor.util.encodeBase64
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json

class JenkinsClient {

    @UseExperimental(UnstableDefault::class)
    val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json.nonstrict).apply {
                setMapper(LastSuccessfulJobBuildInfo::class, LastSuccessfulJobBuildInfo.serializer())
            }
        }
    }

    @UseExperimental(InternalAPI::class)
    suspend fun getLastSuccessfulBuildInfo() : LastSuccessfulJobBuildInfo{
        val userName = "TODO file configuration"
        val password = "TODO file configuration"
        val jenkinsUrl = "TODO file configuration"
        val jobId = "TODO file configuration"

        val buildInfo : LastSuccessfulJobBuildInfo = client.get {
            url(getLastSuccessfulBuildUrl(jenkinsUrl, jobId))
            header("Authorization", "Basic " + "$userName:$password".encodeBase64())
        }
        client.close()

        return buildInfo
    }

    private fun getLastSuccessfulBuildUrl(jenkinsUrl : String, jobId : String) : Url = Url("https://$jenkinsUrl/job/$jobId/lastBuild/api/json?tree=id,building,result,timestamp,duration,estimatedDuration,url")
}

@Serializable
data class LastSuccessfulJobBuildInfo(
        val id: Long,
        val url: String,
        val timestamp: Long,
        val building: Boolean,
        val result: String,
        val duration: Long,
        val estimatedDuration: Long)