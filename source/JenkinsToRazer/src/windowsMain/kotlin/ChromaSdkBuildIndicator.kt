import kotlinx.coroutines.*

private const val COLOR_BLINKING_ON = "fff600"
private const val COLOR_BLINKING_OFF = "000000"
private const val COLOR_STATIC_BUILD_SUCCESS = "00FF00"
private const val COLOR_STATIC_BUILD_FAILED = "0000FF"

class ChromaSdkBuildIndicator(private val razerClient: RazerClient) : BuildIndicator {

    lateinit var blinkJob: Job

    @InternalCoroutinesApi
    suspend fun run(backgroundColorHex: String) {
        razerClient.setBackgroundColor(backgroundColorHex)
        razerClient.run()
    }

    override suspend fun buildStarted() {
        cancelBlinking()
        startBlinking()
    }

    override suspend fun buildFailed() {
        cancelBlinking()
        setStaticColor(COLOR_STATIC_BUILD_SUCCESS)
    }

    override suspend fun buildSucceeded() {
        cancelBlinking()
        setStaticColor(COLOR_STATIC_BUILD_FAILED)
    }

    private suspend fun startBlinking() = coroutineScope{
        blinkJob = launch {
            var on = false

            while (isActive){
                razerClient.setKey(if (on) COLOR_BLINKING_ON else COLOR_BLINKING_OFF, 0, 3)
                on = !on
                delay(500)
            }
        }
    }

    private fun cancelBlinking() {
        if (::blinkJob.isInitialized && blinkJob.isActive) blinkJob.cancel()
    }

    private fun setStaticColor(colorHex: String){
        razerClient.setKey(colorHex, 0, 3)
    }
}