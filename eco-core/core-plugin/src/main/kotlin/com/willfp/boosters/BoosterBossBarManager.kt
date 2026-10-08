package com.willfp.boosters

import com.willfp.boosters.boosters.Booster
import com.willfp.boosters.boosters.activeBoosters
import com.willfp.eco.core.Prerequisite
import org.bukkit.Bukkit
import org.bukkit.boss.BossBar
import java.util.concurrent.ConcurrentHashMap

class BoosterBossBarManager {
    private val bars = ConcurrentHashMap<Booster, BossBar>()

    fun render() {
        if (Prerequisite.HAS_FOLIA.isMet && !Bukkit.isGlobalTickThread()) {
            plugin.scheduler.global().run { render() }
            return
        }

        val activeBoosters = Bukkit.getServer().activeBoosters
            .map { it.booster }
            .toSet()

        val toRemove = bars.keys.filter { it !in activeBoosters || !it.bossBarEnabled }
        for (booster in toRemove) {
            clearFor(booster)
        }

        for (booster in activeBoosters) {
            if (!booster.bossBarEnabled) {
                continue
            }

            val bar = bars.computeIfAbsent(booster) {
                Bukkit.createBossBar(
                    booster.bossBarName,
                    booster.bossBarColor,
                    booster.bossBarStyle
                )
            }

            bar.setTitle(booster.bossBarName)
            bar.color = booster.bossBarColor
            bar.style = booster.bossBarStyle
            bar.progress = booster.bossBarProgress

            val onlinePlayers = Bukkit.getOnlinePlayers().toSet()

            for (player in onlinePlayers) {
                if (!player.isBossBarVisible() || booster.isDisabledIn(player.world)) {
                    if (bar.players.contains(player)) {
                        bar.removePlayer(player)
                    }
                    continue
                }

                if (!bar.players.contains(player)) {
                    bar.addPlayer(player)
                }
            }

            for (player in bar.players.toList()) {
                if (player !in onlinePlayers) {
                    bar.removePlayer(player)
                }
            }
        }
    }

    fun clearFor(booster: Booster) {
        val bar = bars.remove(booster) ?: return
        bar.removeAll()
        bar.isVisible = false
    }

    fun clearAll() {
        for (bar in bars.values) {
            bar.removeAll()
            bar.isVisible = false
        }

        bars.clear()
    }
}

