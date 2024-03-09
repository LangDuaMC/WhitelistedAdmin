package dev.tudubucket.whitelistedadmin;

import dev.tudubucket.whitelistedadmin.commands.WhitelistAdminCommand;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.Objects;
import java.util.logging.Logger;
// import org.bstats.bukkit.Metrics;

public final class main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Logger logger = getLogger();
        File whitelistFolder = new File(getDataFolder(), "WhitelistedAdmin");
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        Objects.requireNonNull(getCommand("wladmin")).setExecutor(new WhitelistAdminCommand(this));

        // Register the listener
        Bukkit.getPluginManager().registerEvents(new WhitelistAdminCommand(this), this);

        // int pluginId = 1234;
        // Metrics metrics = new Metrics(this, pluginId);

    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
