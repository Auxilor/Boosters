package com.willfp.boosters

import com.willfp.boosters.boosters.Boosters
import com.willfp.boosters.commands.CommandBoosters
import com.willfp.boosters.config.DataYml
import com.willfp.boosters.data.SaveHandler
import com.willfp.boosters.data.SaveHandler.Companion.save
import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.event.Listener
import java.io.IOException

class BoostersPlugin : EcoPlugin() {
    val dataYml: DataYml = DataYml(this)

    override fun handleReload() {
        save(this)
        scheduler.runTimer(SaveHandler.Runnable(this), 10000, 20000)

        for (booster in Boosters.values()) {
            this.eventManager.unregisterListener(booster)
            this.eventManager.registerListener(booster)
        }
    }

    override fun handleDisable() {
        try {
            dataYml.save()
        } catch (e: IOException) {
            e.printStackTrace()
        }
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
        return "6.7.0"
    }

    init {
        instance = this
    }

    companion object {
        lateinit var instance: BoostersPlugin
    }
}