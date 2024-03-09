package dev.tudubucket.whitelistedadmin.commands;

import dev.tudubucket.whitelistedadmin.SendMessage;
import dev.tudubucket.whitelistedadmin.DiscordIntegration;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sql.rowset.spi.SyncFactoryException;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class WhitelistAdminCommand implements CommandExecutor, Listener {

    public WhitelistAdminCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private final JavaPlugin plugin;

    public String getConfigMessage(String path, String player, String ipAddress) {
        FileConfiguration config = plugin.getConfig();

        String pattern = Objects.requireNonNull(config.getString("date-format"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        try {
            return Objects.requireNonNull(config.getString(path))
                .replace("{time}", formatter.toString())
                .replace("{player}", player)
                .replace("{current_address}", ipAddress);
        } catch (NullPointerException e) {
            Logger logger = plugin.getLogger();
            logger.warning("Unknown path [" + path + "] while getting config message");
            return path;
        }
        
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

    public void checkNonWhitelisted(Player player, String type, String data) {
        FileConfiguration config = plugin.getConfig();
        Logger logger = plugin.getLogger();
        List<String> permissions = config.getStringList("admin-permissions");
        List<String> ignoredCommandList = config.getStringList("ignore-commands");
        // SendMessage.send(player, String.join(", ", permissions));
        if (config.getString("whitelisted." + player.getName()) == null) {
            for (String permission: permissions) {
                // SendMessage.send(player, permission + ": " + player.hasPermission(permission));
                Boolean performKick = true;
                if (player.hasPermission(permission)) {
                    if (type.contains("COMMAND")) {
                        for (String cmd: ignoredCommandList) {
                            if (cmd.equalsIgnoreCase(data)) {
                                performKick = false;
                                break;
                            }
                        }
                    }
                    if (performKick.equals(true)) {
                        String playerIP = player.getAddress().toString().replace("/", "").split(":")[0];
                        player.kickPlayer(ChatColor.translateAlternateColorCodes('&', getConfigMessage("messages.kick.unknown-admin", player.getName(), Objects.toString(player.getAddress()))));
                        sendAnnouncement(getConfigMessage("messages.integration.unknown-admin", player.getName(), Objects.toString(player.getAddress())));
                        String ftype = "";
                        if (type.equals("JOIN")) {
                            ftype = "login";
                        }
                        else if (type.equals("COMMAND_PRE_PROCESS")) {
                            ftype = "payload command [" + data + "]";
                        }
                        logger.warning("Player " + player.getName() + " (" + playerIP  + ") attempted to " + ftype + " with Administrator permission: " + permission);
                        
                        return;
                    }
                    return;
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            SendMessage.send(sender, "&c&oWhitelistedAdmin &r&eby &6tudubucket");
            // SendMessage.send(sender, "&6For help, type &e/help");
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
            SendMessage.send(sender, "&cUsage: /wladmin + playerName ipAddress");
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
            sendAnnouncement(getConfigMessage("messages.integration.success-whitelisted-ip", args[1], args[2]));
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
            SendMessage.send(sender, "&cUsage: /wladmin - playerName");
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
        Logger logger = plugin.getLogger();
        String player = event.getName();
        String playerIP = event.getAddress().toString().replace("/", "");
        String storedIP = config.getString("whitelisted." + player);

        if (storedIP != null && !storedIP.equals(playerIP)) {
            event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                ChatColor.translateAlternateColorCodes('&', getConfigMessage("messages.kick.invalid-address", player, playerIP))
            );
            logger.warning("Administrator " + player + " attempted to login with non-whitelisted IP Address: " + playerIP);
            sendAnnouncement(getConfigMessage("messages.integration.invalid-address", player, playerIP));
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        checkNonWhitelisted(event.getPlayer(), "JOIN", null);
    }

    @EventHandler
    public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent event) {
        checkNonWhitelisted(event.getPlayer(), "COMMAND_PRE_PROCESS", event.getMessage());
    }

    // @EventHandler
    // public void onPlayerCommandSend(PlayerCommandSendEvent event) {
    //     checkNonWhitelisted(event.getPlayer(), "COMMAND_PAYLOAD", event.getMessage());
    // }
}
