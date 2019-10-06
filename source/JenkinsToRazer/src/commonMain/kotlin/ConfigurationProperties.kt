import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

@Serializable
private data class DeserializedConfigurationProperties(
        val url : String,
        val auth: AuthProperties,
        val pollingIntervalMs: Long,
        val razer: RazerProperties,
        val buildJobs: List<BuildJobProperties>,
        val effects: List<Effect>)

@Serializable
data class AuthProperties(
        val username: String,
        val password: String)

@Serializable
data class RazerProperties(
        val backgroundEffect: String)

@Serializable
private data class BuildJobProperties(
        val id: Int,
        val job: String,
        val branch: String? = null,
        val keyColumn: Int,
        val keyRow: Int,
        val buildInProgressEffect: String,
        val buildSuccessfulEffect: String,
        val buildFailedEffect: String
)

@Serializable
abstract class Effect(@Transient open val id: String = "")

@SerialName("STATIC")
@Serializable
data class StaticEffect(override val id: String, val rgbHex: String) : Effect(id)

@SerialName("BLINK")
@Serializable
data class BlinkEffect(override val id: String, val rgbOnHex: String, val rgbOffHex: String) : Effect(id)

class ConfigurationLoader {
    fun loadConfigurationProperties() : ConfigurationProperties {
        val configurationPropertiesJson = loadConfigurationPropertiesJson()
        val deserializedConfigurationProperties = deserializeConfigurationProperties(configurationPropertiesJson)
        return mapDeserializedConfigurationProperties(deserializedConfigurationProperties)
    }
}

private fun deserializeConfigurationProperties(configurationPropertiesJson : String) : DeserializedConfigurationProperties {
    val effectModule = SerializersModule {
        polymorphic(Effect::class) {
            StaticEffect::class with StaticEffect.serializer()
            BlinkEffect::class with BlinkEffect.serializer()
        }
    }
    return Json(context = effectModule).parse(DeserializedConfigurationProperties.serializer(), configurationPropertiesJson)
}

private fun mapDeserializedConfigurationProperties(inputProperties: DeserializedConfigurationProperties) : ConfigurationProperties {
    val effects  = inputProperties.effects.map { it.id to it }.toMap()

    fun getEffect(effectId: String) : Effect = effects[effectId] ?: throw IllegalArgumentException("Unknown effect $effectId")

    val jobConfiguration = inputProperties.buildJobs.map { it.id to
        JobConfigurationProperties(
            it.job,
            it.branch,
            it.keyRow,
            it.keyColumn,
            getEffect(it.buildInProgressEffect),
            getEffect(it.buildSuccessfulEffect),
            getEffect(it.buildFailedEffect)
    )}.toMap()

    val backgroundEffect = getEffect(inputProperties.razer.backgroundEffect) as? StaticEffect
            ?: throw IllegalArgumentException("Only static background effects are supported at the moment.")

    val razerConfiguration = RazerConfigurationProperties(backgroundEffect)

    return ConfigurationProperties(
            inputProperties.url,
            inputProperties.auth,
            inputProperties.pollingIntervalMs,
            razerConfiguration,
            jobConfiguration)
}

data class ConfigurationProperties (
        val url : String,
        val auth: AuthProperties,
        val pollingIntervalMs: Long,
        val razer: RazerConfigurationProperties,
        val jobs : Map<Int, JobConfigurationProperties>
)

data class RazerConfigurationProperties(
        val backgroundEffect: StaticEffect)

data class JobConfigurationProperties(
        val job: String,
        val branch: String? = null,
        val keyRow: Int,
        val keyColumn: Int,
        val buildInProgressEffect: Effect,
        val buildSuccessfulEffect: Effect,
        val buildFailedEffect: Effect)
