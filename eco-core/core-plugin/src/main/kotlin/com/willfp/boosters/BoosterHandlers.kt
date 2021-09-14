package com.willfp.boosters

import com.willfp.boosters.boosters.Booster
import com.willfp.boosters.boosters.Boosters
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.Server
import org.bukkit.entity.Player

private var active: Booster? = null
private val plugin = BoostersPlugin.instance

var Server.activeBooster: Booster?
    get() {
        return active
    }
    set(value) {
        active = value
    }

val OfflinePlayer.boosters: List<Booster>
    get() {
        val found = mutableListOf<Booster>()
        val section = plugin.dataYml.getSubsectionOrNull("${this.uniqueId}")

        for (key in (section?.getKeys(false) ?: emptyList())) {
            val booster = Boosters.getById(key) ?: continue
            val amount = section?.getIntOrNull(key) ?: continue

            for (i in 0 until amount) {
                found.add(booster)
            }
        }

        return found
    }

fun OfflinePlayer.getAmountOfBooster(booster: Booster): Int {
    return plugin.dataYml.getIntOrNull("${this.uniqueId}.${booster.id}") ?: 0
}


fun OfflinePlayer.setAmountOfBooster(booster: Booster, amount: Int) {
    plugin.dataYml.set("${this.uniqueId}.${booster.id}", amount)
}

fun Player.activateBooster(booster: Booster): Boolean {
    val amount = this.getAmountOfBooster(booster)

    if (amount <= 0) {
        return false
    }

    setAmountOfBooster(booster, amount - 1)

    for (activationMessage in booster.getActivationMessages(this)) {
        Bukkit.broadcastMessage(activationMessage)
    }

    plugin.scheduler.runLater ({
        for (expiryMessage in booster.getExpiryMessages()) {
            Bukkit.broadcastMessage(expiryMessage)
        }
        Bukkit.getServer().activeBooster = null
    }, booster.duration.toLong())

    active = booster

    return true
}

