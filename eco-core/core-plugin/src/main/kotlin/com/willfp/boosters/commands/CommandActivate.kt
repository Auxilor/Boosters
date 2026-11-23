package com.willfp.boosters.commands

import com.willfp.boosters.activateBooster
import com.willfp.boosters.activateBoosterConsole
import com.willfp.boosters.boosters.Boosters
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import wiki.ecohub.core.command.CommandBuilder

private const val TICKS_PER_SECOND = 20

internal fun CommandBuilder<CommandSender>.activateCommand() = subcommand(
    name = "activate",
    permission = "boosters.command.activate"
) {
    executes {
        val booster = argument(index = 0, missingKey = "requires-booster", invalidKey = "invalid-booster") {
            Boosters.getByID(it.lowercase())
        }
        val player = sender as? Player

        val result = if (player == null) {
            Bukkit.getServer().activateBoosterConsole(booster)
        } else {
            player.activateBooster(booster)
        }

        reply(
            result.result.langString,
            "booster" to booster.name,
            "duration" to booster.getFormattedTimeLeft(result.duration.toInt() / TICKS_PER_SECOND)
        )
    }

    completes {
        if (args.size == 1) {
            Boosters.values().map { it.id.key }
        } else {
            emptyList()
        }
    }
}
