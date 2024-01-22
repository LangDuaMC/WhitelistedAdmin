package ankita.tudubucket;

import ankita.tudubucket.commands.WhitelistAdminCommand;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.Objects;
import java.util.logging.Logger;

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
        Objects.requireNonNull(getCommand("whitelist-admin")).setExecutor(new WhitelistAdminCommand(this));

        // Register the listener
        Bukkit.getPluginManager().registerEvents(new WhitelistAdminCommand(this), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            /*
             * We register the EventListener here, when PlaceholderAPI is installed.
             * Since all events are in the main class (this class), we simply use "this"
             */
            Bukkit.getPluginManager().registerEvents(this, this);
        } else {
            /*
             * We inform about the fact that PlaceholderAPI isn't installed and then
             * disable this plugin to prevent issues.
             */
            System.out.print("Could not find PlaceholderAPI! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
