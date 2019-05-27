import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class JenkinsAuth(val username: String, val password: String)

@Serializable
data class JenkinsJob(val id: String)

@Serializable
data class JenkinsConfiguration(val url : String, val auth: JenkinsAuth, val job : JenkinsJob)

@Serializable
data class ConfigurationProperties(val jenkins : JenkinsConfiguration)

class ConfigurationLoader {
    fun loadConfigurationProperties() : ConfigurationProperties {
        val configurationPropertiesJson = loadConfigurationPropertiesJson()
        return Json.parse(ConfigurationProperties.serializer(), configurationPropertiesJson)
    }
}