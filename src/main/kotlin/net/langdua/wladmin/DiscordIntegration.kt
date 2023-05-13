package net.langdua.wladmin

import net.langdua.bootstrap.PluginConfig
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import javax.sql.rowset.spi.SyncFactoryException

class DiscordIntegration(pluginConfig: PluginConfig) {
    private val config = pluginConfig.getConfigurationSection("integrations.discord")?.let { Config.fromConfig(it) }

    class Config(
        val method: String?,
        val token: String?,
        val url: String?,
        val channelId: String?
    ) {
        companion object {
            fun fromConfig(config: ConfigurationSection): Config {
                return Config(
                    config.getString("method"),
                    config.getString("bot-token"),
                    config.getString("webhook-url"),
                    config.getString("alert-channel-id")
                )
            }
        }
    }

    @Throws(IOException::class, SyncFactoryException::class)
    fun sendMessage(message: String) {
        if (config == null) {
            throw IllegalStateException("Improper Discord hook configuration")
        }
        try {
            if ((config.method == "webhook" || config.method == "bot") &&
                (config.method == "bot" && !config.token.isNullOrBlank()) ||
                (config.method == "webhook" && !config.url.isNullOrBlank())
            ) {
                val connection =
                    URL(if (config.method == "bot") "https://discord.com/api/channels/${config.channelId}/messages" else config.url).openConnection() as HttpURLConnection
                if (config.method == "bot") {
                    config.token?.let {
                        connection.setRequestProperty("Authorization", "Bot $it")
                    }
                }
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                val outputStream = connection.outputStream
                val payload =
                    "{\"content\":\"$message\"}" // Both bot token and webhook url are not defined// Sending message using a webhook
                outputStream.write(payload.toByteArray(StandardCharsets.UTF_8))
                outputStream.flush()
                outputStream.close()
                connection.inputStream.close()
                connection.disconnect()
            } else {
                throw IllegalStateException("Neither bot token nor webhook url is defined for Discord integration")
            }
        } catch (e: IOException) {
            val logger = Bukkit.getLogger()
            logger.severe("Failed to send message to Discord: " + e.message)
        }
    }
}
