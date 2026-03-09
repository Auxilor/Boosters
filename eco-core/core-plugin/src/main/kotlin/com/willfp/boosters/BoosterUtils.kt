@file:JvmName("BoosterUtils")

package com.willfp.boosters

import com.willfp.boosters.boosters.ActivatedBooster
import com.willfp.boosters.boosters.ActivationResult
import com.willfp.boosters.boosters.Booster
import com.willfp.boosters.boosters.BoosterActivationResult
import com.willfp.boosters.boosters.BoosterQueue
import com.willfp.boosters.boosters.Boosters
import com.willfp.boosters.boosters.activateBooster
import com.willfp.boosters.boosters.activeBoosters
import com.willfp.boosters.boosters.increaseBooster
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.sound.PlayableSound
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.savedDisplayName
import com.willfp.libreforge.EmptyProvidedHolder
import com.willfp.libreforge.NamedValue
import com.willfp.libreforge.effects.Chain
import com.willfp.libreforge.effects.EffectList
import com.willfp.libreforge.toDispatcher
import com.willfp.libreforge.triggers.TriggerData
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.Server
import org.bukkit.entity.Player
import java.util.UUID

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

val serverUUID = UUID.fromString("0000fff-0000-0000-0000-000000000000")

fun OfflinePlayer.getAmountOfBooster(booster: Booster): Int {
    return this.profile.read(booster.ownedDataKey)
}

fun OfflinePlayer.setAmountOfBooster(booster: Booster, amount: Int) {
    this.profile.write(booster.ownedDataKey, amount)
}

fun OfflinePlayer.incrementBoosters(booster: Booster, amount: Int) {
    this.setAmountOfBooster(booster, this.getAmountOfBooster(booster) + amount)
}

@Suppress("DEPRECATION")
fun Server.activateBoosterConsole(booster: Booster): BoosterActivationResult {
    val consoleName = plugin.langYml
        .getMessage("console-displayname")
        .formatEco(formatPlaceholders = false)

    var effects: Chain?

    var status: ActivationResult

    var newTime = booster.duration.toLong()

    val toMergeWith = Bukkit.getServer().activeBoosters.firstOrNull { it.booster.canBeMerged(booster) }

    val blocking = Bukkit.getServer().activeBoosters.firstOrNull { it.booster.isCategorizedWith(booster)  }

    if (toMergeWith != null) {
        effects = booster.incrementEffects

        newTime += toMergeWith.booster.secondsLeft * 20

        status =  ActivationResult.MERGED
    } else if (blocking != null) {
        val isQueueMerge = BoosterQueue.shouldMergeInQueue(booster)

        effects = if (isQueueMerge > 0) booster.queueIncrementEffects else booster.queueEffects

        status = ActivationResult.QUEUED

        if (isQueueMerge > 0) {
            newTime += isQueueMerge
        }

        BoosterQueue.addBooster(booster, this.consoleSender)
    } else {
        status = ActivationResult.ACTIVATED

        effects = booster.activationEffects
    }

    Bukkit.getOnlinePlayers().forEach { target ->
        effects?.trigger(
            TriggerData(player = target)
                .dispatch(target.toDispatcher())
                .apply {
                    addPlaceholder(NamedValue("activator", consoleName))
                    addPlaceholder(NamedValue("time", booster
                        .getFormattedTimeLeft(newTime.toInt() / 20)))
                }
        )
    }

    if (status == ActivationResult.ACTIVATED) {
        @Suppress("DEPRECATION")
        for (activationCommand in booster.activationCommands) {
            Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                activationCommand.replace("%player%", consoleName)
            )
        }

        for (activationMessage in booster.getActivationMessages(null)) {

            Bukkit.broadcastMessage(activationMessage)
        }

        this.activateBooster(ActivatedBooster(booster, null))

    for (player in Bukkit.getOnlinePlayers()) {
        PlayableSound.create(plugin.configYml.getSubsection("sounds.activate"))?.playTo(player)
    }

    } else if (status == ActivationResult.MERGED) {
        for (incrementCommand in booster.incrementCommands) {
            Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                incrementCommand.replace("%player%", consoleName)
            )
        }
}

@Suppress("DEPRECATION")
fun Server.incrementBoosterConsole(booster: Booster) {
    val consoleName = plugin.langYml
        .getMessage("console-displayname")
        .formatEco(formatPlaceholders = false)

    Bukkit.getOnlinePlayers().forEach { target ->
        booster.incrementEffects?.trigger(
            TriggerData(player = target)
                .dispatch(target.toDispatcher())
                .apply { addPlaceholder(NamedValue("activator", consoleName)) }
        )
    }

    for (incrementCommand in booster.incrementCommands) {
        Bukkit.dispatchCommand(
            Bukkit.getConsoleSender(),
            incrementCommand.replace("%player%", consoleName)
        )
    }

        for (incrementMessage in booster.getIncrementMessage(null)) {
            @Suppress("DEPRECATION")
            Bukkit.broadcastMessage(incrementMessage)
        }

        Bukkit.getServer().increaseBooster(booster.active, booster)

        for (player in Bukkit.getOnlinePlayers()) {
            player.playSound(
                player.location,
                Sound.UI_TOAST_CHALLENGE_COMPLETE,
                2f,
                0.9f
            )
    for (player in Bukkit.getOnlinePlayers()) {
        PlayableSound.create(plugin.configYml.getSubsection("sounds.increment"))?.playTo(player)

        for (incrementMessage in booster.getIncrementMessage(null)) {
            @Suppress("DEPRECATION")
            Bukkit.broadcastMessage(incrementMessage)
        }
    }

    return BoosterActivationResult(status, newTime)
}

@Suppress("DEPRECATION")
fun Player.activateBooster(booster: Booster): BoosterActivationResult {
    val amount = this.getAmountOfBooster(booster)

    if (amount <= 0) {
        return BoosterActivationResult(ActivationResult.INSUFFICIENT_AMOUNT, -1)
    }

    // Condition check

    val activationConditions = booster.activationConditions

    if (!activationConditions.areMet(this.toDispatcher(), EmptyProvidedHolder)) {
        return BoosterActivationResult(ActivationResult.DENIED_CONDITIONS, -1)
    }

    // Decrement booster amount

    this.setAmountOfBooster(booster, amount - 1)

    // Check if should activate, queue, merge or merge in queue

    var effects: Chain? = null

    var status = ActivationResult.ACTIVATED

    var newTime = booster.duration.toLong()

    val toMergeWith = Bukkit.getServer().activeBoosters.firstOrNull { it.booster.canBeMerged(booster) }

    val blocking = Bukkit.getServer().activeBoosters.firstOrNull { it.booster.isCategorizedWith(booster)  }

    if (toMergeWith != null) {
        effects = booster.incrementEffects

        newTime += toMergeWith.booster.secondsLeft * 20

        status =  ActivationResult.MERGED
    } else if (blocking != null) {
        val isQueueMerge = BoosterQueue.shouldMergeInQueue(booster)

        effects = if (isQueueMerge > 0) booster.queueIncrementEffects else booster.queueEffects

        status = ActivationResult.QUEUED

        if (isQueueMerge > 0) {
            newTime += isQueueMerge
        }

        BoosterQueue.addBooster(booster, this)
    } else {
        status = ActivationResult.ACTIVATED

        effects = booster.activationEffects
    }

    val activator = this

    if (status == ActivationResult.ACTIVATED) {
        for (activationMessage in booster.getActivationMessages(this)) {

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
            PlayableSound.create(plugin.configYml.getSubsection("sounds.activate"))?.playTo(player
            )
        }
    } else if (status == ActivationResult.MERGED) {
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

        Bukkit.getServer().increaseBooster(booster.active, booster)
    }

    if (effects != null) {
        Bukkit.getOnlinePlayers().forEach { target ->
            val dispatched = TriggerData(player = target)
                .dispatch(target.toDispatcher())

            dispatched.addPlaceholder(
                NamedValue("activator", activator.name)
            )

            dispatched.addPlaceholder(
                NamedValue("time", booster.getFormattedTimeLeft(newTime.toInt() / 20))
            )

            effects.trigger(dispatched)
        }
    }

    Bukkit.getServer().increaseBooster(booster)

    for (incrementMessage in booster.getIncrementMessage(this)) {
        @Suppress("DEPRECATION")
        Bukkit.broadcastMessage(incrementMessage)
    }

    @Suppress("DEPRECATION")
    for (incrementCommand in booster.incrementCommands) {
        Bukkit.dispatchCommand(
            Bukkit.getConsoleSender(),
            incrementCommand.replace("%player%", this.name)
        )
    }

    return BoosterActivationResult(status, newTime)
}

fun OfflinePlayer.activateQueuedBooster(booster: Booster, time: Long) {
    val player = this.player

    if (player != null) {
        for (activationMessage in booster.getActivationMessages(player)) {
            @Suppress("DEPRECATION")
            Bukkit.broadcastMessage(activationMessage)
        }

        for (activationCommand in booster.activationCommands) {
            Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                activationCommand.replace("%player%", player.name)
            )
        }
    }

    if (booster.activationEffects != null) {
        Bukkit.getOnlinePlayers().forEach { target ->
            val dispatched = TriggerData(player = target)
                .dispatch(target.toDispatcher())

            dispatched.addPlaceholder(
                NamedValue("activator", player?.name ?: this.savedDisplayName)
            )

            dispatched.addPlaceholder(
                NamedValue("time", booster.getFormattedTimeLeft(time.toInt() / 20))
            )

            booster.activationEffects.trigger(dispatched)
        }
    }

    Bukkit.getServer().activateBooster(
        ActivatedBooster(booster, this.uniqueId)
    )

    for (player in Bukkit.getOnlinePlayers()) {
        PlayableSound.create(plugin.configYml.getSubsection("sounds.activate"))?.playTo(player)
    }
}

fun Server.activateQueuedBoosterConsole(booster: Booster, time: Long) {
    val consoleName = BoostersPlugin.instance.langYml
        .getMessage("console-displayname")
        .formatEco(formatPlaceholders = false)

    for (activationMessage in booster.getActivationMessages(null)) {
        @Suppress("DEPRECATION")
        Bukkit.broadcastMessage(activationMessage)
    }

    for (activationCommand in booster.activationCommands) {
        Bukkit.dispatchCommand(
            Bukkit.getConsoleSender(),
            activationCommand.replace("%player%", consoleName)
        )
    }

    if (booster.activationEffects != null) {
        Bukkit.getOnlinePlayers().forEach { target ->
            val dispatched = TriggerData(player = target)
                .dispatch(target.toDispatcher())

            dispatched.addPlaceholder(
                NamedValue("activator", consoleName)
            )

            dispatched.addPlaceholder(
                NamedValue("time", booster.getFormattedTimeLeft(time.toInt() / 20))
            )

            booster.activationEffects.trigger(dispatched)
        }
    }

    this.activateBooster(
        ActivatedBooster(booster, null)
    )

    for (player in Bukkit.getOnlinePlayers()) {
        PlayableSound.create(plugin.configYml.getSubsection("sounds.activate"))?.playTo(player)
    }
}
    }