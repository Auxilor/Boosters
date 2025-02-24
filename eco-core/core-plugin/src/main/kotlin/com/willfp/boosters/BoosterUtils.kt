@file:JvmName("BoosterUtils")

package com.willfp.boosters

import com.willfp.boosters.boosters.ActivatedBooster
import com.willfp.boosters.boosters.Booster
import com.willfp.boosters.boosters.Boosters
import com.willfp.boosters.boosters.activateBooster
import com.willfp.eco.core.data.profile
import com.willfp.eco.util.formatEco
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.Server
import org.bukkit.Sound
import org.bukkit.entity.Player

val OfflinePlayer.boosters: List<Booster>
    get() {
        val found = mutableListOf<Booster>()

        for (booster in Boosters.values()) {
            val amount = this.profile.read(booster.ownedDataKey)
            for (i in 0 until amount) {
                found.add(booster)
            }
        }

        return found
    }

fun OfflinePlayer.getAmountOfBooster(booster: Booster): Int {
    return this.profile.read(booster.ownedDataKey)
}

fun OfflinePlayer.setAmountOfBooster(booster: Booster, amount: Int) {
    this.profile.write(booster.ownedDataKey, amount)
}

fun OfflinePlayer.incrementBoosters(booster: Booster, amount: Int) {
    this.setAmountOfBooster(booster, this.getAmountOfBooster(booster) + amount)
}

fun Server.activateBoosterConsole(booster: Booster) {
    for (activationCommand in booster.activationCommands) {
        Bukkit.dispatchCommand(
            Bukkit.getConsoleSender(),
            activationCommand.replace("%player%", BoostersPlugin.instance.langYml.getMessage("console-displayname").formatEco(formatPlaceholders = false))
        )
    }

    for (activationMessage in booster.getActivationMessages(null)) {
        @Suppress("DEPRECATION")
        Bukkit.broadcastMessage(activationMessage)
    }

    Bukkit.getServer().activateBooster(
        ActivatedBooster(booster, null)
    )

    for (player in Bukkit.getOnlinePlayers()) {
        player.playSound(
            player.location,
            Sound.UI_TOAST_CHALLENGE_COMPLETE,
            2f,
            0.9f
        )
    }
}

fun Player.activateBooster(booster: Booster): Boolean {
    val amount = this.getAmountOfBooster(booster)

    if (amount <= 0) {
        return false
    }

    this.setAmountOfBooster(booster, amount - 1)

    for (activationMessage in booster.getActivationMessages(this)) {
        @Suppress("DEPRECATION")
        Bukkit.broadcastMessage(activationMessage)
    }

    for (activationCommand in booster.activationCommands) {
        Bukkit.dispatchCommand(
            Bukkit.getConsoleSender(),
            activationCommand.replace("%player%", this.name)
        )
    }

    Bukkit.getServer().activateBooster(
        ActivatedBooster(booster, this.uniqueId)
    )

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
