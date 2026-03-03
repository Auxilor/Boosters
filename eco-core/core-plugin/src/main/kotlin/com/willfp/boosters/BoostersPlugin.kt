package com.willfp.boosters

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
import com.willfp.libreforge.toDispatcher
import org.bukkit.Bukkit
import org.bukkit.Sound

class BoostersPlugin : LibreforgePlugin() {
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

        BoosterQueue.loadQueue()

        this.scheduler.runTimer(20L, 20L) {     // was 1,1 → now 20,20
            for (booster in Boosters.values()) {
                if (booster.active == null) {
                    continue
                }

                if (booster.secondsLeft <= 0) {
                    for (expiryMessage in booster.expiryMessages) {
                        @Suppress("DEPRECATION")
                        Bukkit.broadcastMessage(expiryMessage)
                    }

                    for (expiryCommand in booster.expiryCommands) {
                        Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            expiryCommand.replace("%player%", booster.active?.player?.name ?: "")
                        )
                    }

                    Bukkit.getOnlinePlayers().forEach { player ->
                        booster.expiryEffects?.trigger(player.toDispatcher())

                        player.playSound(
                            player.location,
                            Sound.ENTITY_ENDER_DRAGON_DEATH,
                            2f,
                            0.9f
                        )
                    }

                    Bukkit.getServer().expireBooster(booster)

                    // Check the queue

                    val queued = BoosterQueue.popBooster(booster)

                    if (queued != null) {
                        val activator = queued.activator

                        if (activator == serverUUID) {
                            Bukkit.getServer().activateQueuedBoosterConsole(queued.booster,
                                queued.duration.toLong())
                        } else {
                            val player = Bukkit.getOfflinePlayer(activator)
                            player.activateQueuedBooster(queued.booster,
                                queued.duration.toLong())
                        }
                    }
                }
            }
        }

        // Just run it later enough
        this.scheduler.runLater(3) {
            Bukkit.getServer().scanForBoosters()
        }
    }

    override fun handleDisable() {
        BoosterQueue.saveQueue()
    }

    override fun loadPluginCommands(): List<PluginCommand> {
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
