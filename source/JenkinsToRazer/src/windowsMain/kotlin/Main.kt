import kotlinx.cinterop.*
import kotlinx.coroutines.*
import platform.posix.*

@InternalCoroutinesApi
fun main(){
    runBlocking {
        val razerClient = RazerClient()
        razerClient.init()

        launch {
            razerClient.run()
        }

        launch {
            val colorValues = listOf(16711680, 65280, 255)
            while (isActive){
                razerClient.setColor(colorValues.shuffled().first())
                delayOnPlatform(1000)
            }
        }
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

@UseExperimental(ExperimentalUnsignedTypes::class)
actual suspend fun delayOnPlatform(timeMillis: Long) {
    memScoped {
        val timespec = alloc<timespec>()
        timespec.tv_sec = timeMillis / 1000
        timespec.tv_nsec = ((timeMillis % 1000L) * 1000000L).convert()
        nanosleep(timespec.ptr, null)
    }
}
