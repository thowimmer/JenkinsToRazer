import kotlinx.coroutines.runBlocking

fun main(){
    runBlocking {
        val client = JenkinsClient()
        val buildInfo = client.getLastSuccessfulBuildInfo()
        print(buildInfo)
    }
}