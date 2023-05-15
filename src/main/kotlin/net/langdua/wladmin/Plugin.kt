package net.langdua.wladmin

import net.langdua.bootstrap.PluginBootstrap
import net.langdua.bootstrap.Utility
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
    var utility = Utility(this)
    override fun onEnable() {
        Metrics(this, BuildConfig.BSTATS_ID)
        if (!configFile.exists()) saveDefaultConfig()
        config.options().copyDefaults(true)
        saveDefaultConfig()
        YamlConfiguration.loadConfiguration(configFile)
        Objects.requireNonNull(getCommand("whitelist-admin"))?.setExecutor(WhitelistAdminCommand(this))
        // Register the listener
        Bukkit.getPluginManager().registerEvents(WhitelistAdminCommand(this), this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
