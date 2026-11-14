package com.willfp.boosters.commands

import com.willfp.boosters.activateQueuedBooster
import com.willfp.boosters.activateQueuedBoosterConsole
import com.willfp.boosters.boosters.ActivatedBooster
import com.willfp.boosters.boosters.BoosterExpiryWarnings
import com.willfp.boosters.boosters.BoosterQueue
import com.willfp.boosters.boosters.activeBoosters
import com.willfp.boosters.boosters.boosterLock
import com.willfp.boosters.boosters.expireBooster
import com.willfp.boosters.bossBarManager
import com.willfp.boosters.runExpiryEffects
import com.willfp.boosters.serverUUID
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import wiki.ecohub.core.command.CommandBuilder
import wiki.ecohub.core.command.CommandContext

private val modes = listOf("all", "booster", "category")

private val silentCompletions = listOf("silent")

private const val SILENT_ARGS = 3

internal fun CommandBuilder<CommandSender>.cancelCommand() = subcommand(
    name = "cancel",
    permission = "boosters.command.cancel"
) {
    executes {
        val mode = argument(0, "cancel-usage").lowercase()

        synchronized(boosterLock) {
            when (mode) {
                "all" -> cancelAll()
                "booster" -> cancelBooster()
                "category" -> cancelCategory()
                else -> fail("invalid-command")
            }
        }

        reply("cancelled")
    }

    completes {
        when {
            args.size == 1 -> modes
            args.size == 2 -> secondArgCompletions(args[0].lowercase())
            args.size == SILENT_ARGS && args[0].lowercase() in listOf("booster", "category") -> silentCompletions
            else -> emptyList()
        }
    }
}

private fun secondArgCompletions(mode: String): List<String> = when (mode) {
    "all" -> silentCompletions
    "booster" -> Bukkit.getServer().activeBoosters.map { it.booster.id.key }
    "category" -> Bukkit.getServer().activeBoosters.mapNotNull { it.booster.category }.distinct()
    else -> emptyList()
}

private fun CommandContext<CommandSender>.isSilent(index: Int): Boolean =
    args.getOrNull(index)?.equals("silent", true) == true

private fun expire(booster: ActivatedBooster, silent: Boolean) {
    if (!silent) {
        booster.booster.runExpiryEffects()
    }

    bossBarManager.clearFor(booster.booster)
    BoosterExpiryWarnings.remove(booster.booster)
    Bukkit.getServer().expireBooster(booster.booster)
}

private fun CommandContext<CommandSender>.cancelAll() {
    val silent = isSilent(1)

    for (booster in Bukkit.getServer().activeBoosters) {
        expire(booster, silent)
    }

    BoosterQueue.queue.clear()
    BoosterQueue.saveQueue()
}

private fun CommandContext<CommandSender>.cancelBooster() {
    val boosterId = argument(1, "requires-booster").lowercase()
    val target = Bukkit.getServer().activeBoosters.firstOrNull { it.booster.id.key.equals(boosterId, true) }
        ?: fail("invalid-active-booster")

    expire(target, isSilent(2))

    val queued = BoosterQueue.popBooster(target.booster) ?: return

    if (queued.activator == serverUUID) {
        Bukkit.getServer().activateQueuedBoosterConsole(queued.booster, queued.duration.toLong())
    } else {
        Bukkit.getOfflinePlayer(queued.activator).activateQueuedBooster(queued.booster, queued.duration.toLong())
    }
}

private fun CommandContext<CommandSender>.cancelCategory() {
    val categoryId = argument(1, "requires-category").lowercase()
    val targets = Bukkit.getServer().activeBoosters.filter { it.booster.category.equals(categoryId, true) }

    if (targets.isEmpty()) {
        fail("invalid-active-category")
    }

    val silent = isSilent(2)

    for (target in targets) {
        expire(target, silent)
    }

    BoosterQueue.queue.remove(categoryId)
    BoosterQueue.saveQueue()
}
