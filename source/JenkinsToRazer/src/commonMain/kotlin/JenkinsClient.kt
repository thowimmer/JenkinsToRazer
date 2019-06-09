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

class JenkinsClient(private val config : ConfigurationProperties) {

    @UseExperimental(UnstableDefault::class)
    val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json.nonstrict).apply {
                setMapper(BuildInfo::class, BuildInfo.serializer())
            }
        }
    }

    @UseExperimental(InternalAPI::class)
    suspend fun getLatestBuildInfo(jobId: String, branch: String?) : BuildInfo{
        return client.get {
            url(getLastSuccessfulBuildUrl(config.url, jobId, branch))
            header("Authorization", "Basic " + "${config.auth.username}:${config.auth.password}".encodeBase64())
        }
    }

    private fun getLastSuccessfulBuildUrl(jenkinsUrl : String, jobId : String, branch: String?) : Url {
        val multiBranchJob = if(branch.isNullOrEmpty()) "" else "/job/$branch"
        return Url("https://$jenkinsUrl/job/$jobId$multiBranchJob/lastBuild/api/json?tree=id,building,result,timestamp,duration,estimatedDuration,url")
    }
}

@Serializable
data class BuildInfo(
        val id: Long,
        val url: String,
        val timestamp: Long,
        val building: Boolean,
        val result: String?,
        val duration: Long,
        val estimatedDuration: Long)