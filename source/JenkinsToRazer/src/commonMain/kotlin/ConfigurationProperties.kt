import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

@Serializable
data class ConfigurationProperties(
        val url : String,
        val auth: AuthProperties,
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
data class BuildJobProperties(
        val id: Int,
        val job: String,
        val branch: String,
        val keyColumn: Int,
        val keyRow: Int,
        val buildInProgressEffect: String,
        val buildSuccessfulEffect: String,
        val buildFailedEffect: String
)

@Serializable
abstract class Effect(@Transient open val id: String)

@SerialName("STATIC")
@Serializable
data class StaticEffect(override val id: String, val rgbHex: String) : Effect(id)

@SerialName("BLINK")
@Serializable
data class BlinkEffect(override val id: String, val rgbOnHex: String, val rgbOffHex: String) : Effect(id)

class ConfigurationLoader {
    fun loadConfigurationProperties() : ConfigurationProperties {
        val effectModule = SerializersModule {
            polymorphic(Effect::class) {
                StaticEffect::class with StaticEffect.serializer()
                BlinkEffect::class with BlinkEffect.serializer()
            }
        }
        val configurationPropertiesJson = loadConfigurationPropertiesJson()
        return Json(context = effectModule).parse(ConfigurationProperties.serializer(), configurationPropertiesJson)
    }
}