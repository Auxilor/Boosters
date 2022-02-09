package com.willfp.boosters

import com.willfp.boosters.boosters.Boosters
import com.willfp.boosters.commands.CommandBoosters
import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.core.integrations.placeholder.PlaceholderEntry
import com.willfp.eco.core.integrations.placeholder.PlaceholderManager
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.savedDisplayName
import org.bukkit.Bukkit
import org.bukkit.event.Listener

class BoostersPlugin : EcoPlugin() {
    override fun handleEnable() {
        PlaceholderManager.registerPlaceholder(
            PlaceholderEntry(
                this,
                "booster_info",
                {
                    val booster = server.activeBooster

                    if (booster == null) {
                        return@PlaceholderEntry "&cThere is no booster currently active!"
                            .formatEco(formatPlaceholders = false)
                    } else {
                        return@PlaceholderEntry "${Bukkit.getOfflinePlayer(booster.player).savedDisplayName} &fhas activated a &a${booster.booster.name}&f!"
                            .formatEco(formatPlaceholders = false)
                    }
                },
                false
            )
        )
    }

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