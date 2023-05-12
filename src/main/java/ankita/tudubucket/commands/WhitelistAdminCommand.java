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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sql.rowset.spi.SyncFactoryException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class WhitelistAdminCommand implements CommandExecutor, Listener {

    private final JavaPlugin plugin;

    public WhitelistAdminCommand(JavaPlugin plugin) {
        this.plugin = plugin;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            SendMessage.send(sender, "&c&oWhitelistedAdmin &r&eby &6tudubucket");
            SendMessage.send(sender, "&6For help, type &e/help");
            return false;
        }

        if (args[0].equals("reload-config")) {
            // Reload the config
            plugin.reloadConfig();
            SendMessage.send(sender, "Configuration reloaded.");
            return true;
        }

        if (args[0].equals("+") && args.length == 3) {
            // Add player to the whitelist
            String playerName = args[1];
            String ip = args[2];
            FileConfiguration config = plugin.getConfig();
            config.set("whitelisted." + playerName, ip);
            try {
                config.save(new File(plugin.getDataFolder(), "config.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            DiscordIntegration Discord = new DiscordIntegration(plugin.getConfig());
            SendMessage.send(sender, playerName + " has been added to the whitelist.");
            if(Objects.equals(config.getBoolean("integrations.discord.enabled"), true)) {
                try {
                    Discord.sendMessage(config.getString("messages.integration.success-whitelisted-admin"));
                } catch (IOException | SyncFactoryException e) {
                    throw new RuntimeException(e);
                }
            }
            plugin.reloadConfig();
            return true;
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
                DiscordIntegration Discord = new DiscordIntegration(plugin.getConfig());
                SendMessage.send(sender, playerName + " has been removed from the whitelist.");
                if(Objects.equals(config.getBoolean("integrations.discord.enabled"), true)) {
                    try {
                        Discord.sendMessage(config.getString("messages.integration.success-removed-admin"));
                    } catch (IOException | SyncFactoryException e) {
                        throw new RuntimeException(e);
                    }
                }
                plugin.reloadConfig();
            } else {
                SendMessage.send(sender, playerName + " is not in the whitelist.");
                plugin.reloadConfig();
            }
            return true;
        }

        SendMessage.send(sender, "&cInvalid command!");
        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) throws SyncFactoryException, IOException {
        // Get the configuration
        FileConfiguration config = plugin.getConfig();

        // Check if the player has any forbidden permissions
        List<String> permissions = config.getStringList("permissions");
        Player player = event.getPlayer();
        String checkValid = config.getString("whitelisted." + player);
        for (String permission: permissions) {
            if (player.hasPermission(permission) && checkValid == null) {
                player.kickPlayer(config.getString("messages.kick.unknown-admin"));
                if(Objects.equals(config.getBoolean("integrations.discord.enabled"), true)) {
                    try {
                        DiscordIntegration Discord = new DiscordIntegration(plugin.getConfig());
                        Discord.sendMessage(config.getString("messages.integration.unknown-admin"));
                    } catch (IOException | SyncFactoryException e) {
                        throw new RuntimeException(e);
                    }
                }
                return;
            }
        }

        // Check if the player is whitelisted
        String playerName = player.getName();
        String playerIP = Objects.requireNonNull(player.getAddress()).getHostString();
        String storedIP = config.getString("whitelisted." + playerName);
        if (storedIP != null && !storedIP.equals(playerIP)) {
            player.kickPlayer(config.getString("messages.kick.invalid-address"));
            if(Objects.equals(config.getBoolean("integrations.discord.enabled"), true)) {
                DiscordIntegration Discord = new DiscordIntegration(plugin.getConfig());
                Discord.sendMessage(config.getString("messages.integration.invalid-address"));
            }
        }
    }

}
