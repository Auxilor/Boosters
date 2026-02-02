@file:JvmName("BoosterUtils")

package com.willfp.boosters

import com.willfp.boosters.boosters.ActivatedBooster
import com.willfp.boosters.boosters.Booster
import com.willfp.boosters.boosters.Boosters
import com.willfp.boosters.boosters.activateBooster
import com.willfp.boosters.boosters.increaseBooster
import com.willfp.eco.core.data.profile
import com.willfp.eco.util.formatEco
import com.willfp.libreforge.NamedValue
import com.willfp.libreforge.toDispatcher
import com.willfp.libreforge.triggers.DispatchedTrigger
import com.willfp.libreforge.triggers.TriggerData
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
    val consoleName = BoostersPlugin.instance.langYml
        .getMessage("console-displayname")
        .formatEco(formatPlaceholders = false)

    for (activationCommand in booster.activationCommands) {
        Bukkit.dispatchCommand(
            Bukkit.getConsoleSender(),
            activationCommand.replace("%player%", consoleName)
        )
    }

    for (activationMessage in booster.getActivationMessages(null)) {
        @Suppress("DEPRECATION")
        Bukkit.broadcastMessage(activationMessage)
    }

    Bukkit.getOnlinePlayers().forEach { target ->
        booster.activationEffects?.trigger(
            TriggerData(player = target)
                .dispatch(target.toDispatcher())
                .apply { addPlaceholder(NamedValue("activator", consoleName)) }
        )
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


fun Player.increaseBooster(booster: Booster): Boolean {
    val amount = this.getAmountOfBooster(booster)

    if (amount <= 0) {
        return false
    }

    this.setAmountOfBooster(booster, amount - 1)

    val activator = this
    val effects = booster.incrementEffects

    if (effects != null) {
        Bukkit.getOnlinePlayers().forEach { target ->
            val dispatched = TriggerData(player = target)
                .dispatch(target.toDispatcher())

            dispatched.addPlaceholder(
                NamedValue("activator", activator.name)
            )

            effects.trigger(dispatched)
        }
    }

    Bukkit.getServer().increaseBooster(booster.active, booster)

    for (incrementMessage in booster.getIncrementMessage(this)) {
        @Suppress("DEPRECATION")
        Bukkit.broadcastMessage(incrementMessage)
    }

    for (incrementCommand in booster.incrementCommands) {
        Bukkit.dispatchCommand(
            Bukkit.getConsoleSender(),
            incrementCommand.replace("%player%", this.name)
        )
    }

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

    val activator = this

    Bukkit.getOnlinePlayers().forEach { target ->
        val dispatched = TriggerData(player = target)
            .dispatch(target.toDispatcher())

        dispatched.addPlaceholder(
            NamedValue("activator", activator.name)
        )

        booster.activationEffects?.trigger(dispatched)
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
