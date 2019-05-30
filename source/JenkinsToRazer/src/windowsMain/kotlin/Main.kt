import kotlinx.cinterop.*
import kotlinx.coroutines.*
import platform.posix.*

@InternalCoroutinesApi
fun main(){
    runBlocking {
        val razerClient = RazerClient()
        razerClient.init()
        razerClient.setBackgroundColor("333333")

        launch {
            razerClient.run()
        }

        launch {
            var on = false
            while (isActive){
                razerClient.setKey(if (on) "00FF00" else "000000", 0, 3)
                on = !on
                delay(500)
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
