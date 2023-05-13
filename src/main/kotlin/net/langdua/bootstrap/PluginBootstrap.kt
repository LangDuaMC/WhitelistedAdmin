package net.langdua.bootstrap

import net.kyori.adventure.text.minimessage.MiniMessage
import net.langdua.main.BuildConfig
import org.bstats.bukkit.Metrics
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

abstract class PluginBootstrap : JavaPlugin(), Listener {
    val mm = MiniMessage.miniMessage()
    val metrics = Metrics(this, BuildConfig.BSTATS_ID)

    // Implement
    val config = this.getConfig() as PluginConfig
}
