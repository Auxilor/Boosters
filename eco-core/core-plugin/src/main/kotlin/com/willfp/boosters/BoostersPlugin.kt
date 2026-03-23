package com.willfp.boosters

import com.willfp.boosters.boosters.BoosterBossBar
import com.willfp.boosters.boosters.BoosterQueue
import com.willfp.boosters.boosters.Boosters
import com.willfp.boosters.boosters.activeBoosters
import com.willfp.boosters.boosters.expireBooster
import com.willfp.boosters.boosters.scanForBoosters
import com.willfp.boosters.commands.CommandBoosters
import com.willfp.boosters.libreforge.ConditionIsBoosterActive
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.libreforge.SimpleProvidedHolder
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import com.willfp.libreforge.registerGenericHolderProvider
import org.bukkit.Bukkit

internal lateinit var plugin: BoostersPlugin
    private set

class BoostersPlugin : LibreforgePlugin() {
    init {
        plugin = this
    }

    override fun loadConfigCategories(): List<ConfigCategory> {
        return listOf(
            Boosters
        )
    }

    override fun handleEnable() {
        Conditions.register(ConditionIsBoosterActive)

        registerGenericHolderProvider {
            Bukkit.getServer().activeBoosters.map { it.booster }.map { SimpleProvidedHolder(it) }
        }

        BoosterQueue.loadQueue()
    }

    override fun handleReload() {
        BoosterQueue.saveQueue()
        BoosterBossBar.clearAll()
        BoosterQueue.loadQueue()
        this.scheduler.runTimer(20L, 20L) {
            for (booster in Boosters.values()) {
                if (booster.active == null) {
                    continue
                }

                if (booster.secondsLeft <= 0) {
                    booster.runExpiryEffects()

                    BoosterBossBar.clearFor(booster)
                    Bukkit.getServer().expireBooster(booster)

                    // Check the queue

                    val queued = BoosterQueue.popBooster(booster)

                    if (queued != null) {
                        val activator = queued.activator

                        if (activator == serverUUID) {
                            Bukkit.getServer().activateQueuedBoosterConsole(
                                queued.booster,
                                queued.duration.toLong()
                            )
                        } else {
                            val player = Bukkit.getOfflinePlayer(activator)
                            player.activateQueuedBooster(
                                queued.booster,
                                queued.duration.toLong()
                            )
                        }
                    }
                }
            }

            BoosterBossBar.render()
        }

        // Just run it later enough
        this.scheduler.runLater(3) {
            Bukkit.getServer().scanForBoosters()
            BoosterBossBar.render()
        }
    }

    override fun handleDisable() {
        BoosterBossBar.clearAll()
        BoosterQueue.saveQueue()
    }

    override fun loadPluginCommands(): List<PluginCommand> {
        return listOf(
            CommandBoosters
        )
    }
}
