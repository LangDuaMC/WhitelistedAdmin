package net.langdua.wladmin

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

class SendMessage {
    companion object {
        fun send(sender: CommandSender?, message: String) {
            val msg =
                ChatColor.translateAlternateColorCodes(
                    '&',
                    PlaceholderAPI.setPlaceholders(
                        if (sender is Player && sender !is ConsoleCommandSender) sender else null,
                        message
                    )
                )
            if (sender != null) {
                sender.sendMessage(msg)
            } else {
                Bukkit.getServer().consoleSender.sendMessage(msg)
            }
        }
    }
}
