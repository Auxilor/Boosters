package com.willfp.boosters.boosters

import com.willfp.boosters.plugin
import com.willfp.boosters.serverUUID
import com.willfp.eco.core.config.emptyConfig
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.ServerProfile
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.util.savedDisplayName
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.util.UUID

object BoosterQueue {
    val queue = mutableMapOf<String, MutableList<QueuedBooster>>()

    val queuePDK = PersistentDataKey(
        plugin.namespacedKeyFactory.create("booster-queue"),
        PersistentDataKeyType.CONFIG,
        emptyConfig()
    )

    fun shouldMergeInQueue(booster: Booster): Int {
        val category = booster.category ?: return -1
        if (!queue.containsKey(category)) return -1
        val currentQueue = queue[category] ?: return -1
        if (currentQueue.isEmpty()) return -1
        val lastBooster = currentQueue.last()
        return if (lastBooster.booster.canBeMerged(booster)) lastBooster.duration else -1
    }

    fun addBooster(booster: Booster, activator: CommandSender) {
        val category = booster.category

        if (category == null) {
            plugin.logger.warning { "Tried queueing ${booster.id.key} without a category" }
            return
        }

        if (!queue.containsKey(category)) {
            queue[category] = mutableListOf(QueuedBooster(booster, booster.duration, activator.uniqueId))
        } else {
            val currentQueue = queue[category]!!
            if (currentQueue.isEmpty()) {
                currentQueue.add(QueuedBooster(booster, booster.duration, activator.uniqueId))
            } else {
                val lastBooster = currentQueue.last()
                if (lastBooster.booster.canBeMerged(booster)) {
                    // If the last booster in the queue is the same as the new one, increase its duration
                    currentQueue[currentQueue.size - 1] = QueuedBooster(
                        booster,
                        lastBooster.duration + booster.duration, activator.uniqueId
                    )
                } else {
                    // Otherwise, add the new booster to the end of the queue
                    currentQueue.add(QueuedBooster(booster, booster.duration, activator.uniqueId))
                }
            }

            queue[category] = currentQueue
        }

        saveQueue()
    }

    fun popBooster(previous: Booster): QueuedBooster? {
        val category = previous.category ?: return null
        if (!queue.containsKey(category)) return null
        val currentQueue = queue[category]!!
        if (currentQueue.isEmpty()) return null

        val next = currentQueue.removeAt(0)
        queue[category] = currentQueue
        saveQueue()
        return next
    }

    fun serializeQueue(): Config {
        val base = emptyConfig()

        for ((category, boosters) in queue) {
            base.set(category, boosters.map { "${it.booster.id.key}__${it.duration}__${it.activator}" })
        }

        return base
    }

    fun deserializeQueue(config: Config) {
        for (key in config.getKeys(false)) {
            val boosterStrings = config.getStrings(key) ?: continue
            val boosters = boosterStrings.mapNotNull {
                val split = it.split("__")
                val booster = Boosters.getByID(split[0]) ?: return@mapNotNull null
                val duration = split[1].toIntOrNull() ?: return@mapNotNull null
                val uuid = UUID.fromString(split[2])
                QueuedBooster(booster, duration, uuid)
            }.toMutableList()
            queue[key] = boosters
        }
    }

    fun saveQueue() {
        val config = serializeQueue()
        ServerProfile.load().write(queuePDK, config)
    }

    fun loadQueue() {
        val config = ServerProfile.load().read(queuePDK)
        queue.clear()
        deserializeQueue(config)

        val count = queue.values.sumOf { it.size }

        plugin.logger.info { "Loaded $count queued boosters" }
    }
}

data class QueuedBooster(
    val booster: Booster,
    val duration: Int,
    val activator: UUID
) {
    fun getActivatorName(): String {
        if (this.activator == serverUUID) {
            return plugin.langYml.getFormattedString("console-displayname")
        }
        val player = Bukkit.getOfflinePlayer(activator)
        return player.player?.name ?: player.savedDisplayName
    }
}

val CommandSender.uniqueId: UUID
    get() {
        return if (this is org.bukkit.entity.Player) {
            this.uniqueId
        } else {
            serverUUID
        }
    }