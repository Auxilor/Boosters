package com.willfp.boosters

import com.willfp.boosters.boosters.Boosters
import com.willfp.boosters.boosters.activeBoosters
import com.willfp.boosters.boosters.expireBooster
import com.willfp.boosters.boosters.scanForBoosters
import com.willfp.boosters.commands.CommandBoosters
import com.willfp.boosters.config.BoostersYml
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.core.data.ServerProfile
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.integrations.placeholder.PlaceholderManager
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.savedDisplayName
import com.willfp.libreforge.LibReforgePlugin
import org.bukkit.Bukkit

import org.bukkit.event.Listener
import kotlin.math.floor

class BoostersPlugin : LibReforgePlugin(supportsLrcdb = false) {
    val boostersYml = BoostersYml(this)

    override fun handleEnableAdditional() {
        this.registerHolderProvider { Bukkit.getServer().activeBoosters.map { it.booster } }
    }

    override fun handleReloadAdditional() {
        this.scheduler.runTimer(1, 1) {
            for (booster in Boosters.values()) {
                if (booster.active == null) {
                    continue
                }

                if (booster.secondsLeft <= 0) {
                    for (expiryMessage in booster.expiryMessages) {
                        Bukkit.broadcastMessage(expiryMessage)
                    }

                    for (expiryCommand in booster.expiryCommands) {
                        Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            expiryCommand.replace("%player%", booster.active?.player?.name ?: "")
                        )
                    }

                    Bukkit.getServer().expireBooster(booster)
                }
            }
        }

        // Just run it later enough
        this.scheduler.runLater(3) {
            Bukkit.getServer().scanForBoosters()
        }
    }

    override fun loadListeners(): List<Listener> {
        return listOf(

        )
    }

    override fun loadPluginCommands(): List<com.willfp.eco.core.command.impl.PluginCommand> {
        return listOf(
            CommandBoosters(this)
        )
    }

    init {
        instance = this
    }

    companion object {
        lateinit var instance: BoostersPlugin
    }
}
