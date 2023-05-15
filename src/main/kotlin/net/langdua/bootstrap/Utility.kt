package net.langdua.bootstrap

import net.kyori.adventure.text.Component

class Utility(private val plugin: PluginBootstrap) {
    fun getAdventureComponent(name: String): Component? {
        return plugin.config.getString(name)?.let { plugin.mm.deserialize(it) }
    }
}
