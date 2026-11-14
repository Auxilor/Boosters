package com.willfp.boosters.commands

import com.willfp.boosters.boosters.BoosterQueue
import com.willfp.boosters.boosters.Boosters
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import wiki.ecohub.core.command.CommandBuilder
import wiki.ecohub.ui.text.formatEco

private const val TICKS_PER_SECOND = 20

internal fun CommandBuilder<CommandSender>.queueCommand() = subcommand(
    name = "queue",
    permission = "boosters.command.queue"
) {
    executes {
        val category = argument(0, "requires-category").lowercase()
        val player = sender as? Player

        val queued = synchronized(BoosterQueue.queue) { BoosterQueue.queue[category]?.toList() }.orEmpty()

        val format = if (queued.isEmpty()) {
            plugin.langYml.getStrings("empty-queue-format")
        } else {
            plugin.langYml.getStrings("queue-format")
        }

        val lines = format.flatMap { line ->
            if (line.contains("%number%")) {
                queued.mapIndexed { index, booster ->
                    line.replace(oldValue = "%number%", newValue = (index + 1).toString())
                        .replace(oldValue = "%booster%", newValue = booster.booster.name)
                        .replace(
                            oldValue = "%time%",
                            newValue = booster.booster.getFormattedTimeLeft(booster.duration / TICKS_PER_SECOND)
                        )
                        .replace(oldValue = "%activator%", newValue = booster.getActivatorName())
                        .replace(oldValue = "%category%", newValue = category)
                }
            } else {
                listOf(line.replace(oldValue = "%category%", newValue = category))
            }
        }

        lines.forEach { sender.sendMessage(it.formatEco(player, true)) }
    }

    completes {
        if (args.size == 1) {
            Boosters.getCategories()
        } else {
            emptyList()
        }
    }
}
