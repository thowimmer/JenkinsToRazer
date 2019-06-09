import kotlinx.coroutines.*

class ChromaSdkBuildIndicator(private val razerClient: RazerClient, private val config: ConfigurationProperties) : BuildIndicator {

    private val activeBlinkEffects : MutableMap<Int, BlinkEffectConfiguration> = mutableMapOf()

    @InternalCoroutinesApi
    suspend fun run() {
        coroutineScope {

            razerClient.setBackgroundColor(config.razer.backgroundEffect.rgbHex)

            launch {
                razerClient.run()
            }

            launch {
                var on = false

                while (isActive) {
                    for(blinkConfig in activeBlinkEffects.values){
                        razerClient.setKey(
                                if (on) blinkConfig.blinkEffect.rgbOnHex else blinkConfig.blinkEffect.rgbOnHex,
                                blinkConfig.keyRow,
                                blinkConfig.keyColumn)
                    }
                    on = !on
                    delay(500)
                }
            }
        }
    }

    override suspend fun buildStarted(buildJobId: Int) {
        val jobConfig = config.jobs.getValue(buildJobId)
        setEffect(buildJobId, jobConfig.keyRow, jobConfig.keyColumn, jobConfig.buildInProgressEffect)
    }

    override suspend fun buildFailed(buildJobId: Int) {
        val jobConfig = config.jobs.getValue(buildJobId)
        setEffect(buildJobId, jobConfig.keyRow, jobConfig.keyColumn, jobConfig.buildFailedEffect)
    }

    override suspend fun buildSucceeded(buildJobId: Int) {
        val jobConfig = config.jobs.getValue(buildJobId)
        setEffect(buildJobId, jobConfig.keyRow, jobConfig.keyColumn, jobConfig.buildSuccessfulEffect)
    }

    private fun setEffect(buildJobId: Int, keyRow : Int, keyColumn: Int, effect: Effect) {
       when(effect){
           is StaticEffect -> {
               activeBlinkEffects.remove(buildJobId)
               razerClient.setKey(effect.rgbHex, keyRow, keyColumn)
           }

           is BlinkEffect -> {
               activeBlinkEffects[buildJobId] = BlinkEffectConfiguration(keyRow, keyColumn, effect)
           }
       }
    }
}

private class BlinkEffectConfiguration(
        val keyRow: Int,
        val keyColumn: Int,
        val blinkEffect: BlinkEffect)