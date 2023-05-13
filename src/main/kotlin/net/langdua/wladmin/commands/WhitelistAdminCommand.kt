package net.langdua.wladmin.commands

import net.langdua.wladmin.DiscordIntegration
import net.langdua.wladmin.Plugin
import net.langdua.wladmin.SendMessage
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.io.File
import java.io.IOException
import java.util.*
import javax.sql.rowset.spi.SyncFactoryException

class WhitelistAdminCommand(private val plugin: Plugin) : CommandExecutor, Listener {
    private val config = plugin.config
    private val discord = DiscordIntegration(config)
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            SendMessage.send(sender, "&c&oWhitelistedAdmin &r&eby &6tudubucket")
            SendMessage.send(sender, "&6For help, type &e/help")
            return false
        }
        if (args[0] == "reload-config") {
            // Reload the config
            plugin.reloadConfig()
            SendMessage.send(sender, "Configuration reloaded.")
            return true
        }
        if (args[0] == "+" && args.size == 3) {
            // Add player to the whitelist
            val playerName = args[1]
            val ip = args[2]
            config["whitelisted.$playerName"] = ip
            try {
                config.save(File(plugin.dataFolder, "config.yml"))
            } catch (e: IOException) {
                e.printStackTrace()
            }
            SendMessage.send(sender, "$playerName has been added to the whitelist.")
            if (config.getBoolean("integrations.discord.enabled")) {
                try {
                    discord.sendMessage(config.getString("messages.integration.success-whitelisted-admin")!!)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                } catch (e: SyncFactoryException) {
                    throw RuntimeException(e)
                }
            }
            plugin.reloadConfig()
            return true
        }
        if (args[0] == "-" && args.size == 2) {
            // Remove player from the whitelist
            val playerName = args[1]

            if (config.contains("whitelisted.$playerName")) {
                config["whitelisted.$playerName"] = null
                try {
                    config.save(File(plugin.dataFolder, "config.yml"))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                SendMessage.send(sender, "$playerName has been removed from the whitelist.")
                if (config.getBoolean("integrations.discord.enabled")) {
                    try {
                        discord.sendMessage(config.getString("messages.integration.success-removed-admin")!!)
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    } catch (e: SyncFactoryException) {
                        throw RuntimeException(e)
                    }
                }
                plugin.reloadConfig()
            } else {
                SendMessage.send(sender, "$playerName is not in the whitelist.")
                plugin.reloadConfig()
            }
            return true
        }
        SendMessage.send(sender, "&cInvalid command!")
        return false
    }

    @EventHandler
    @Throws(SyncFactoryException::class, IOException::class)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        // Get the configuration

        // Check if the player has any forbidden permissions
        val permissions = config.getStringList("permissions")
        val player = event.player
        val checkValid = config.getString("whitelisted.$player")
        for (permission in permissions) {
            if (player.hasPermission(permission!!) && checkValid == null) {
                player.kick(config.getAdventureComponent("messages.kick.unknown-admin"))
                if (config.getBoolean("integrations.discord.enabled")) {
                    try {
                        discord.sendMessage(config.getString("messages.integration.unknown-admin")!!)
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    } catch (e: SyncFactoryException) {
                        throw RuntimeException(e)
                    }
                }
                return
            }
        }

        // Check if the player is whitelisted
        val playerName = player.name
        val playerIP = Objects.requireNonNull(player.address).hostString
        val storedIP = config.getString("whitelisted.$playerName")
        if (storedIP != null && storedIP != playerIP) {
            player.kick(config.getAdventureComponent("messages.kick.invalid-address"))
            if (config.getBoolean("integrations.discord.enabled")) {
                discord.sendMessage(config.getString("messages.integration.invalid-address")!!)
            }
        }
    }
}
