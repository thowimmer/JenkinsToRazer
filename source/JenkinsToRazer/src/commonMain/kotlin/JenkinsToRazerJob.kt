import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay

class JenkinsToRazerJob(
        private val jenkinsClient: JenkinsClient,
        private val buildIndicator: BuildIndicator,
        private val config: ConfigurationProperties){

    private var buildJobInfoCache : MutableMap<Int, BuildInfo> = mutableMapOf()

    @InternalCoroutinesApi
    suspend fun run(){
        while (isActive){

            println("Checking build status...")

            config.buildJobs.forEach { buildJob ->
                val newBuildInfo = jenkinsClient.getLatestBuildInfo(buildJob.job, buildJob.branch)
                val currentBuildInfo = buildJobInfoCache[buildJob.id]

                if (currentBuildInfo != null && currentBuildInfo.id == newBuildInfo.id) {
                    println("No Update for BuildJob ${buildJob.id}")
                    publishUpdatedBuildStatus(buildJob.id, currentBuildInfo, newBuildInfo)
                } else {
                    println("Update for BuildJob ${buildJob.id}")
                    publishNewBuildStatus(buildJob.id, newBuildInfo)
                }

                buildJobInfoCache[buildJob.id] = newBuildInfo
            }

            delay(config.pollingIntervalMs)
        }
    }

    private suspend fun publishUpdatedBuildStatus(buildJobId: Int, oldBuildInfo: BuildInfo, newBuildInfo: BuildInfo) {
        when {
            oldBuildInfo.building && !newBuildInfo.building && newBuildInfo.result == "SUCCESS" -> buildIndicator.buildSucceeded(buildJobId)
            oldBuildInfo.building && !newBuildInfo.building && newBuildInfo.result == "UNSTABLE" -> buildIndicator.buildFailed(buildJobId)
        }
    }

    private suspend fun publishNewBuildStatus(buildJobId: Int, newBuildInfo: BuildInfo) {
        when {
            newBuildInfo.building -> buildIndicator.buildStarted(buildJobId)
            newBuildInfo.result == "SUCCESS" -> buildIndicator.buildSucceeded(buildJobId)
            newBuildInfo.result == "UNSTABLE" -> buildIndicator.buildFailed(buildJobId)
        }
    }
}

interface BuildIndicator{
    suspend fun buildStarted(buildJobId: Int)
    suspend fun buildFailed(buildJobId: Int)
    suspend fun buildSucceeded(buildJobId: Int)
}