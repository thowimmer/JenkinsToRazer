import io.ktor.client.HttpClient
import io.ktor.client.request.get

class JenkinsClient {

    private val client = HttpClient()

    suspend fun execute() {
        val htmlContent = client.get<String>("https://en.wikipedia.org/wiki/Main_Page")
        print(htmlContent)
        client.close()
    }
}