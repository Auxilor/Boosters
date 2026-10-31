package com.willfp.boosters

import com.willfp.boosters.boosters.BoosterExpiryWarnings
import com.willfp.boosters.boosters.BoosterQueue
import com.willfp.boosters.boosters.Booster
import com.willfp.boosters.boosters.Boosters
import com.willfp.boosters.boosters.activeBoosters
import com.willfp.boosters.boosters.boosterLock
import com.willfp.boosters.boosters.expireBooster
import com.willfp.boosters.boosters.refreshBoosterData
import com.willfp.boosters.boosters.scanForBoosters
import com.willfp.boosters.commands.CommandBoosters
import com.willfp.boosters.libreforge.ConditionIsBoosterActive
import com.willfp.eco.core.bstats.EcoMetricsChart
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.core.scheduling.EcoTask
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
    private var tickTask: EcoTask? = null
    private var syncTask: EcoTask? = null

    @Volatile
    private var isAuthority = true

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

        registerGenericHolderProvider { dispatcher ->
            Bukkit.getServer().activeBoosters.map { it.booster }
                .filterNot { booster -> dispatcher.location?.world?.let { booster.isDisabledIn(it) } ?: false }
                .map { SimpleProvidedHolder(it) }
        }
    }

    override fun handleReload() {
        BoosterQueue.saveQueue()
        bossBarManager.clearAll()
        BoosterExpiryWarnings.clear()
        BoosterQueue.loadQueue()

        val usesLocalStorage = configYml.getBool("use-local-storage")
        isAuthority = usesLocalStorage || configYml.getBoolOrNull("booster-authority") != false

        tickTask?.cancel()
        tickTask = this.scheduler.global().runTimer(20L, 20L) {
            synchronized(boosterLock) {
                tickBoosters()
            }

            bossBarManager.render()
        }

        syncTask?.cancel()
        syncTask = if (usesLocalStorage) {
            null
        } else {
            this.scheduler.async().runTimer(SYNC_INTERVAL, SYNC_INTERVAL) {
                Bukkit.getServer().refreshBoosterData()
                BoosterQueue.refreshQueueData()

                this.scheduler.global().run {
                    Bukkit.getServer().scanForBoosters()
                    BoosterQueue.syncQueue()
                }
            }
        }

        // Just run it later enough
        this.scheduler.global().runLater(3) {
            Bukkit.getServer().scanForBoosters()
            bossBarManager.render()
        }
    }

    private fun tickBoosters() {
        for (booster in Boosters.values()) {
            if (booster.active == null) {
                continue
            }

            if (booster.secondsLeft > 0) {
                BoosterExpiryWarnings.tick(booster)
            } else if (isAuthority) {
                expire(booster)
            }
        }
    }

    private fun expire(booster: Booster) {
        booster.runExpiryEffects()

        bossBarManager.clearFor(booster)
        BoosterExpiryWarnings.remove(booster)
        Bukkit.getServer().expireBooster(booster)

        // Check the queue

        val queued = BoosterQueue.popBooster(booster) ?: return
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

    override fun handleDisable() {
        tickTask?.cancel()
        tickTask = null
        syncTask?.cancel()
        syncTask = null
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
        },
        EcoMetricsChart.SimplePie("expiry_warning_sound_enabled") {
            if (configYml.getBool("sounds.expiry-warning.enabled")) "enabled" else "disabled"
        }
    )

    private companion object {
        const val SYNC_INTERVAL = 100L
    }
}
