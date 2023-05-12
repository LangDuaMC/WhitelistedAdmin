package net.langdua.wladmin

import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import javax.sql.rowset.spi.SyncFactoryException

class DiscordIntegration(config: FileConfiguration) {
    private val sendMethod: String?
    private val botToken: String?
    private val webhookUrl: String?
    private val alertChannelId: String?

    init {
        val discordConfig = config.getConfigurationSection("integrations.discord")!!
        sendMethod = discordConfig.getString("method")
        botToken = discordConfig.getString("bot-token")
        webhookUrl = discordConfig.getString("webhook-url")
        alertChannelId = discordConfig.getString("alert-channel-id")
    }

    @Throws(IOException::class, SyncFactoryException::class)
    fun sendMessage(message: String) {
        try {
            if ((sendMethod == "webhook" || sendMethod == "bot") &&
                (sendMethod == "bot" && !botToken.isNullOrBlank()) ||
                (sendMethod == "webhook" && !webhookUrl.isNullOrBlank())
            ) {
                val connection =
                    URL(if (sendMethod == "bot") "https://discord.com/api/channels/$alertChannelId/messages" else webhookUrl).openConnection() as HttpURLConnection
                if (sendMethod == "bot") botToken?.let {
                    connection.setRequestProperty("Authorization", "Bot $it")
                }
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                val outputStream = connection.outputStream
                val payload =
                    "{\"content\":\"$message\"}"// Both bot token and webhook url are not defined// Sending message using a webhook
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
