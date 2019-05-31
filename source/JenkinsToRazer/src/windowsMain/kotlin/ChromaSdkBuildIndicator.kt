import kotlinx.coroutines.*

private const val COLOR_BLINKING_ON = "ffff00"
private const val COLOR_BLINKING_OFF = "000000"
private const val COLOR_STATIC_BUILD_SUCCESS = "00ff00"
private const val COLOR_STATIC_BUILD_FAILED = "ff0000"

class ChromaSdkBuildIndicator(private val razerClient: RazerClient) : BuildIndicator {

    private lateinit var blinkJob : Job
    private lateinit var buildIndicatorScope: CoroutineScope

    @InternalCoroutinesApi
    suspend fun run(backgroundColorHex: String) {
        coroutineScope {
            buildIndicatorScope = this

            razerClient.setBackgroundColor(backgroundColorHex)

            launch {
                razerClient.run()
            }
        }
    }

    override suspend fun buildStarted() {
        cancelBlinking()
        startBlinking()
    }

    override suspend fun buildFailed() {
        cancelBlinking()
        setStaticColor(COLOR_STATIC_BUILD_FAILED)
    }

    override suspend fun buildSucceeded() {
        cancelBlinking()
        setStaticColor(COLOR_STATIC_BUILD_SUCCESS)
    }

    private fun startBlinking() {
        blinkJob = buildIndicatorScope.launch {
            var on = false

            while (isActive) {
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