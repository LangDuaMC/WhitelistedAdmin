package net.langdua.wladmin

import net.langdua.bootstrap.PluginBootstrap
import net.langdua.main.BuildConfig
import net.langdua.wladmin.commands.WhitelistAdminCommand
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

class Plugin : PluginBootstrap() {
    val whitelistFolder = File(dataFolder, "WhitelistedAdmin")
    private val configFile = File(dataFolder, "config.yml")
    override fun onEnable() {
        val metrics = Metrics(this, BuildConfig.BSTATS_ID)
        if (!configFile.exists()) saveDefaultConfig()
        config.options().copyDefaults(true)
        saveDefaultConfig()
        YamlConfiguration.loadConfiguration(configFile)
        Objects.requireNonNull(getCommand("whitelist-admin"))?.setExecutor(WhitelistAdminCommand(this))
        // Register the listener
        Bukkit.getPluginManager().registerEvents(WhitelistAdminCommand(this), this)
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            /*
             * We register the EventListener here, when PlaceholderAPI is installed.
             * Since all events are in the main class (this class), we simply use "this"
             */
            Bukkit.getPluginManager().registerEvents(this, this)
        } else {
            /*
             * We inform about the fact that PlaceholderAPI isn't installed and then
             * disable this plugin to prevent issues.
             */
            print("Could not find PlaceholderAPI! This plugin is required.")
            Bukkit.getPluginManager().disablePlugin(this)
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
