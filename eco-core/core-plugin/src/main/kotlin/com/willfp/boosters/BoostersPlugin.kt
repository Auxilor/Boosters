package com.willfp.boosters

import com.willfp.boosters.commands.CommandBoosters
import com.willfp.boosters.config.BoostersYml
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.core.integrations.placeholder.PlaceholderEntry
import com.willfp.eco.core.integrations.placeholder.PlaceholderManager
import com.willfp.eco.util.ListUtils
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.savedDisplayName
import com.willfp.libreforge.LibReforgePlugin
import org.bukkit.Bukkit
import org.bukkit.event.Listener

class BoostersPlugin : LibReforgePlugin(0, 14269, "&e") {
    val boostersYml = BoostersYml(this)

    override fun handleEnableAdditional() {
        PlaceholderManager.registerPlaceholder(
            PlaceholderEntry(
                this,
                "booster_info",
                {
                    val booster = activeBooster

                    if (booster == null) {
                        return@PlaceholderEntry this.langYml.getString("no-currently-active")
                            .formatEco(formatPlaceholders = false)
                    } else {
                        return@PlaceholderEntry this.langYml.getString("active-placeholder")
                            .replace("%player%", Bukkit.getOfflinePlayer(booster.player).savedDisplayName)
                            .replace("%booster%", booster.booster.name)
                            .formatEco(formatPlaceholders = false)
                    }
                },
                false
            )
        )

        this.registerHolderProvider { ListUtils.toSingletonList(activeBooster?.booster) }
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
