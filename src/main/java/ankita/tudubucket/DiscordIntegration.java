package ankita.tudubucket;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import javax.sql.rowset.spi.SyncFactoryException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getLogger;

public class DiscordIntegration {

    private final String sendMethod;
    private final String botToken;
    private final String webhookUrl;
    private final String alertChannelId;

    public DiscordIntegration(FileConfiguration config) {
        ConfigurationSection discordConfig = config.getConfigurationSection("integrations.discord");
        assert discordConfig != null;
        sendMethod = discordConfig.getString("method");
        botToken = discordConfig.getString("bot-token");
        webhookUrl = discordConfig.getString("webhook-url");
        alertChannelId = discordConfig.getString("alert-channel-id");
    }

    public void sendMessage(String message) throws IOException, SyncFactoryException {
        try {
            String payload = "{\"content\":\"" + message + "\"}";
            if (!sendMethod.equals("webhook") && !sendMethod.equals("bot")) {
                throw new IllegalStateException("Invalid sending message method! Please check your configuration file again.");
            } else if (sendMethod.equals("bot") && (botToken != null)) {
                // Sending message using a bot
                String url = "https://discord.com/api/channels/" + alertChannelId + "/messages";
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bot " + botToken);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(payload.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                outputStream.close();
                connection.getInputStream().close();
                connection.disconnect();
            } else if (sendMethod.equals("webhook") && (webhookUrl != null)) {
                // Sending message using a webhook
                HttpURLConnection connection = (HttpURLConnection) new URL(webhookUrl).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(payload.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                outputStream.close();
                connection.getInputStream().close();
                connection.disconnect();
            } else {
                // Both bot token and webhook url are not defined
                throw new IllegalStateException("Neither bot token nor webhook url is defined for Discord integration");
            }
        } catch (IOException e) {
            Logger logger = getLogger();
            logger.severe("Failed to send message to Discord: " + e.getMessage());
        }
    }
}
