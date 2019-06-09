import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import platform.posix.fclose
import platform.posix.fgets
import platform.posix.fopen
import platform.posix.getenv

@InternalCoroutinesApi
fun main(){
    runBlocking {
        val configurationLoader = ConfigurationLoader()
        val configProperties = configurationLoader.loadConfigurationProperties()
        val jenkinsClient = JenkinsClient(configProperties)

        val chromaSdkBuildIndicator = ChromaSdkBuildIndicator(RazerClient(), configProperties)
        launch { chromaSdkBuildIndicator.run() }

        val jenkinsToRazerJob = JenkinsToRazerJob(jenkinsClient, chromaSdkBuildIndicator, configProperties)
        launch { jenkinsToRazerJob.run() }
    }
}

actual fun loadConfigurationPropertiesJson(): String = readTextContent(getConfigurationFilePath())

fun getConfigurationFilePath() : String {
    val userHome = getenv("USERPROFILE")?.toKString() ?: throw IllegalStateException("Userhome not found.")
    return "$userHome\\jenkins2razer.json"
}

//TODO There is no way to create a common cinterop library. This requires code duplication for POSIX file reading.
fun readTextContent(filePath :String) : String {

    val filePointer = fopen(filePath, "r") ?: throw IllegalStateException("Cannot find $filePath")

    val stringBuilder = StringBuilder()

    try {
        memScoped {
            val bufferLength = 64 * 1024
            val buffer = allocArray<ByteVar>(bufferLength)


            var line : String? = fgets(buffer, bufferLength, filePointer)?.toKString()

            while (!line.isNullOrEmpty()) {
                stringBuilder.appendln(line)
                line = fgets(buffer, bufferLength, filePointer)?.toKString()
            }
        }
    } finally {
        fclose(filePointer)
    }

    return stringBuilder.toString()
}