import kotlinx.coroutines.runBlocking

fun main(){
    runBlocking {
        val client = JenkinsClient()
        client.execute()
    }
}