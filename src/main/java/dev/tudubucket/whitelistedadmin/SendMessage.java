package dev.tudubucket.whitelistedadmin;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SendMessage {

    public static void send(CommandSender sender, String message) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String msg = PlaceholderAPI.setPlaceholders(player, message);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        } else {
            String msg = PlaceholderAPI.setPlaceholders(null, message);
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }
    }
}