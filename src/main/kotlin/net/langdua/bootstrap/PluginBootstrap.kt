package net.langdua.bootstrap

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

abstract class PluginBootstrap: JavaPlugin(), Listener {
    val mm = MiniMessage.miniMessage()
    val conf: ConfigManager = config as ConfigManager
}