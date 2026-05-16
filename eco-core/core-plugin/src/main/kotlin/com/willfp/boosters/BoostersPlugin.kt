package com.willfp.boosters

import com.willfp.boosters.boosters.BoosterQueue
import com.willfp.boosters.boosters.Boosters
import com.willfp.boosters.boosters.activeBoosters
import com.willfp.boosters.boosters.expireBooster
import com.willfp.boosters.boosters.scanForBoosters
import com.willfp.boosters.commands.CommandBoosters
import com.willfp.boosters.libreforge.ConditionIsBoosterActive
import com.willfp.eco.core.bstats.EcoMetricsChart
import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.scheduler.BukkitTask
import com.willfp.libreforge.SimpleProvidedHolder
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import com.willfp.libreforge.registerGenericHolderProvider
import org.bukkit.Bukkit

internal lateinit var plugin: BoostersPlugin
    private set

internal lateinit var bossBarManager: BoosterBossBarManager
    private set

class BoostersPlugin : LibreforgePlugin() {
    private var tickTask: BukkitTask? = null

    init {
        plugin = this
        bossBarManager = BoosterBossBarManager()
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
        bossBarManager.clearAll()
        BoosterQueue.loadQueue()

        tickTask?.cancel()
        tickTask = this.scheduler.runTimer(20L, 20L) {
            for (booster in Boosters.values()) {
                if (booster.active == null) {
                    continue
                }

                if (booster.secondsLeft <= 0) {
                    booster.runExpiryEffects()

                    bossBarManager.clearFor(booster)
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

            bossBarManager.render()
        }

        // Just run it later enough
        this.scheduler.runLater(3) {
            Bukkit.getServer().scanForBoosters()
            bossBarManager.render()
        }
    }

    override fun handleDisable() {
        tickTask?.cancel()
        tickTask = null
        bossBarManager.clearAll()
        BoosterQueue.saveQueue()
    }

    override fun loadPluginCommands(): List<PluginCommand> {
        return listOf(
            CommandBoosters
        )
    }

    override fun getCustomCharts() = listOf(
        EcoMetricsChart.SingleLine("total_boosters") { Boosters.values().size },
        EcoMetricsChart.SingleLine("active_boosters") { Bukkit.getServer().activeBoosters.size },
        EcoMetricsChart.SimplePie("use_local_storage") {
            if (configYml.getBool("use-local-storage")) "local" else "shared"
        },
        EcoMetricsChart.SimplePie("activate_sound_enabled") {
            if (configYml.getBool("sounds.activate.enabled")) "enabled" else "disabled"
        },
        EcoMetricsChart.SimplePie("increment_sound_enabled") {
            if (configYml.getBool("sounds.increment.enabled")) "enabled" else "disabled"
        },
        EcoMetricsChart.SimplePie("expire_sound_enabled") {
            if (configYml.getBool("sounds.expire.enabled")) "enabled" else "disabled"
        }
    )
}
