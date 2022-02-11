@file:JvmName("BoosterHandlers")

package com.willfp.boosters

import com.willfp.boosters.boosters.ActivatedBooster
import com.willfp.boosters.boosters.Booster
import com.willfp.boosters.boosters.Boosters
import com.willfp.eco.core.data.PlayerProfile
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.entity.Player

private var active: ActivatedBooster? = null
private val plugin = BoostersPlugin.instance

var activeBooster: ActivatedBooster?
    get() {
        return active
    }
    set(value) {
        active = value
    }

val OfflinePlayer.boosters: List<Booster>
    get() {
        val found = mutableListOf<Booster>()

        for (booster in Boosters.values()) {
            val amount = PlayerProfile.load(this).read(booster.dataKey)
            for (i in 0 until amount) {
                found.add(booster)
            }
        }

        return found
    }

fun OfflinePlayer.getAmountOfBooster(booster: Booster): Int {
    return PlayerProfile.load(this).read(booster.dataKey)
}


fun OfflinePlayer.setAmountOfBooster(booster: Booster, amount: Int) {
    PlayerProfile.load(this).write(booster.dataKey, amount)
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

    plugin.scheduler.runLater(booster.duration.toLong()) {
        for (expiryMessage in booster.expiryMessages) {
            Bukkit.broadcastMessage(expiryMessage)
        }
        activeBooster = null
    }

    active = ActivatedBooster(booster, this.uniqueId)

    for (player in Bukkit.getOnlinePlayers()) {
        player.playSound(
            player.location,
            Sound.UI_TOAST_CHALLENGE_COMPLETE,
            2f,
            0.9f
        )
    }

    return true
}
