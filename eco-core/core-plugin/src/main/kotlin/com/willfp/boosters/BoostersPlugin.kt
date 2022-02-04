package com.willfp.boosters

import com.willfp.boosters.boosters.Boosters
import com.willfp.boosters.commands.CommandBoosters
import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.event.Listener

class BoostersPlugin : EcoPlugin() {
    override fun handleReload() {
        for (booster in Boosters.values()) {
            this.eventManager.unregisterListener(booster)
            this.eventManager.registerListener(booster)
        }
    }

    override fun handleDisable() {
    }

    override fun loadListeners(): List<Listener> {
        return listOf(

        )
    }

    override fun loadPluginCommands(): List<PluginCommand> {
        return listOf(
            CommandBoosters(this)
        )
    }

    override fun getMinimumEcoVersion(): String {
        return "6.24.0"
    }

    init {
        instance = this
    }

    companion object {
        lateinit var instance: BoostersPlugin
    }
}