package net.langdua.wladmin

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SendMessage {
    companion object {
        fun send(sender: CommandSender?, message: String) {
            val msg =
                ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(sender as Player?, message))
            if (sender != null) {
                sender.sendMessage(msg)
            } else {
                Bukkit.getServer().consoleSender.sendMessage(msg)
            }
        }
    }
}
