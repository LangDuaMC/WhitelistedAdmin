package net.langdua.wladmin

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SendMessage {
    companion object {
        fun send(sender: CommandSender, message: String) {
            if (sender is Player) {
                val msg: String = PlaceholderAPI.setPlaceholders(sender, message)
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg))
            } else {
                val msg: String = PlaceholderAPI.setPlaceholders(null, message)
                Bukkit.getServer().consoleSender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg))
            }
        }
    }
}