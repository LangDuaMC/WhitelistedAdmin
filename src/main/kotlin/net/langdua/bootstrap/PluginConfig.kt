package net.langdua.bootstrap

import net.kyori.adventure.text.Component
import net.langdua.wladmin.Plugin
import org.bukkit.configuration.file.FileConfiguration

class PluginConfig(private val plugin: Plugin) : FileConfiguration() {
    override fun saveToString(): String {
        TODO("Not yet implemented")
    }

    override fun loadFromString(contents: String) {
        TODO("Not yet implemented")
    }

    override fun buildHeader(): String {
        TODO("Not yet implemented")
    }

    fun getAdventureComponent(name: String): Component? {
        return getString(name)?.let { plugin.mm.deserialize(it) }
    }
}
