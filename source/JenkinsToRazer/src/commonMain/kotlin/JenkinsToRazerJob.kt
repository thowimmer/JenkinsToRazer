import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay

private const val JENKINS_POLLING_INTERVAL_MILLIS = 2000L

class JenkinsToRazerJob(
        private val jenkinsClient: JenkinsClient,
        private val buildIndicator: BuildIndicator){

    private lateinit var currentBuildInfo : LastJobBuildInfo

    @InternalCoroutinesApi
    suspend fun run(){
        while (isActive){
            val newJobInfo = jenkinsClient.getBuildInfoOfLastJob()

            if (::currentBuildInfo.isInitialized && currentBuildInfo.id == newJobInfo.id) {
                println("Same job")
                publishUpdatedBuildStatus(currentBuildInfo, newJobInfo)
            } else {
                println("New Job detected")
                publishNewBuildStatus(newJobInfo)
            }

            currentBuildInfo = newJobInfo

            delay(JENKINS_POLLING_INTERVAL_MILLIS)
        }
    }

    private suspend fun publishUpdatedBuildStatus(oldJobInfo: LastJobBuildInfo, newJobInfo: LastJobBuildInfo) {
        when {
            oldJobInfo.building && newJobInfo.result == "SUCCESS" -> buildIndicator.buildSucceeded()
            oldJobInfo.building && newJobInfo.result == "UNSTABLE" -> buildIndicator.buildFailed()
        }
    }

    private suspend fun publishNewBuildStatus(newJobInfo: LastJobBuildInfo) {
        when {
            newJobInfo.building -> buildIndicator.buildStarted()
            newJobInfo.result == "SUCCESS" -> buildIndicator.buildSucceeded()
            newJobInfo.result == "UNSTABLE" -> buildIndicator.buildFailed()
        }
    }
}

interface BuildIndicator{
    suspend fun buildStarted()
    suspend fun buildFailed()
    suspend fun buildSucceeded()
}