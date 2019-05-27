import kotlinx.cinterop.*
import kotlinx.coroutines.runBlocking
import platform.posix.*

fun main(){
    runBlocking {
        val configurationLoader = ConfigurationLoader()
        val configProperties = configurationLoader.loadConfigurationProperties()
        val client = JenkinsClient(configProperties.jenkins)
        val buildInfo = client.getBuildInfoOfLastJob()
        print(buildInfo)
    }
}

actual fun loadConfigurationPropertiesJson(): String = readTextContent(getConfigurationFilePath())

fun getConfigurationFilePath() : String {
    val passwd = getpwuid(getuid())
    val userHome = passwd?.pointed?.pw_dir?.toKString() ?: throw IllegalStateException("Userhome not found.")
    return "$userHome/jenkins2razer.json"
}

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

@UseExperimental(ExperimentalUnsignedTypes::class)
actual suspend fun delayOnPlatform(timeMillis: Long) {
    usleep(timeMillis.convert<__useconds_t>() * 1000U)
}