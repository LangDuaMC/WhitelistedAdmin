package ankita.tudubucket.commands;

import ankita.tudubucket.SendMessage;
import ankita.tudubucket.DiscordIntegration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sql.rowset.spi.SyncFactoryException;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class WhitelistAdminCommand implements CommandExecutor, Listener {

    public WhitelistAdminCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public String getConfigMessage(String path, String player, String ipAddress) {
        FileConfiguration config = plugin.getConfig();

        String pattern = Objects.requireNonNull(config.getString("date-format"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        return Objects.requireNonNull(config.getString(path))
                .replace("{time}", formatter.toString())
                .replace("{player}", player)
                .replace("{current_address}", ipAddress);
    }

    public static boolean isValidIPv4(String ip) {
        String[] octets = ip.split("\\.");

        if (octets.length != 4) {
            return false;
        }

        for (String octet : octets) {
            try {
                int num = Integer.parseInt(octet);
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }

    private final JavaPlugin plugin;

    public void sendAnnouncement(String message) {

        FileConfiguration config = plugin.getConfig();
        DiscordIntegration Discord = new DiscordIntegration(config);

        if (Objects.equals(config.getBoolean("integrations.discord.enable"), true)) {
            try {
                Discord.sendMessage(message);
            } catch (IOException | SyncFactoryException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            SendMessage.send(sender, "&c&oWhitelistedAdmin &r&eby &6tudubucket");
            SendMessage.send(sender, "&6For help, type &e/help");
            return false;
        }

        if (args[0].equals("reload")) {
            // Reload the config
            plugin.reloadConfig();
            SendMessage.send(sender, "Configuration reloaded.");
            return true;
        }
        if (args[0].equals("+") && args.length != 3) {
            if (args.length < 3) {
                SendMessage.send(sender, "&cToo few arguments.");
            }
            else {
                SendMessage.send(sender, "&cToo many arguments.");
            }
            SendMessage.send(sender, "&cUsage: /whitelist-admin + playerName ipAddress");
            return false;
        }
        if (args[0].equals("+")) {
            // Add player to the whitelist
            String playerName = args[1];
            String ip = args[2];
            if (!isValidIPv4(ip)) {
                SendMessage.send(sender, ip + " is not a valid IPv4 address.");
                return false;
            }
            FileConfiguration config = plugin.getConfig();
            config.set("whitelisted." + playerName, ip);
            try {
                config.save(new File(plugin.getDataFolder(), "config.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            SendMessage.send(sender, playerName + " has been added to the whitelist.");
            sendAnnouncement(getConfigMessage("messages.integration.success-whitelisted-admin", args[1], args[2]));
            plugin.reloadConfig();
            return true;
        }
        if (args[0].equals("-") && args.length != 2) {
            if (args.length < 2) {
                SendMessage.send(sender, "&cToo few arguments.");
            }
            else {
                SendMessage.send(sender, "&cToo many arguments.");
            }
            SendMessage.send(sender, "&cUsage: /whitelist-admin - playerName");
        }
        if (args[0].equals("-") && args.length == 2) {
            // Remove player from the whitelist
            String playerName = args[1];
            FileConfiguration config = plugin.getConfig();
            if (config.contains("whitelisted." + playerName)) {
                config.set("whitelisted." + playerName, null);
                try {
                    config.save(new File(plugin.getDataFolder(), "config.yml"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                SendMessage.send(sender, playerName + " has been removed from the whitelist.");
                sendAnnouncement(getConfigMessage("messages.integration.success-removed-admin", args[1], "null"));
                plugin.reloadConfig();
            } else {
                SendMessage.send(sender, playerName + " does not in the whitelist.");
                plugin.reloadConfig();
            }
            return true;
        }

        SendMessage.send(sender, "&cInvalid command name!");
        return false;
    }

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        FileConfiguration config = plugin.getConfig();

        String player = event.getName();
        String playerIP = event.getAddress().toString();
        String storedIP = config.getString("whitelisted." + player);

        if (storedIP != null && !storedIP.equals(playerIP)) {
            event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                getConfigMessage("messages.kick.invalid-address", player, playerIP)
            );

            sendAnnouncement(getConfigMessage("messages.integration.invalid-address", player, playerIP));
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        FileConfiguration config = plugin.getConfig();

        List<String> permissions = config.getStringList("admin-permissions");
        Player player = event.getPlayer();
        // SendMessage.send(player, String.join(", ", permissions));
        if (config.getString("whitelisted." + player.getName()) == null) {
            for (String permission: permissions) {
                // SendMessage.send(player, permission + ": " + player.hasPermission(permission));
                if (player.hasPermission(permission)) {
                    player.kickPlayer(getConfigMessage("messages.kick.unknown-admin", player.getName(), Objects.toString(player.getAddress())));
                    sendAnnouncement(getConfigMessage("messages.integration.unknown-admin", player.getName(), Objects.toString(player.getAddress())));
                    return;
                }
            }
        }
    }
}
