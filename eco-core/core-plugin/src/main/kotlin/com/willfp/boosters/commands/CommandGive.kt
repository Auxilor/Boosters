package com.willfp.boosters.commands

import com.willfp.boosters.boosters.Boosters
import com.willfp.boosters.incrementBoosters
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import wiki.ecohub.core.command.CommandBuilder
import wiki.ecohub.ui.text.StringUtils

private val amountCompletions = listOf("1", "2", "3", "4", "5", "10")

private const val AMOUNT_ARGS = 3

internal fun CommandBuilder<CommandSender>.giveCommand() = subcommand(
    name = "give",
    permission = "boosters.command.give"
) {
    executes {
        val playerName = argument(0, "requires-player")
        val booster = argument(index = 1, missingKey = "requires-booster", invalidKey = "invalid-booster") {
            Boosters.getByID(it.lowercase())
        }
        val amount = args.getOrNull(2)?.toIntOrNull() ?: 1

        val player = Bukkit.getPlayerExact(playerName)
            ?: Bukkit.getOfflinePlayers().firstOrNull { it.name.equals(playerName, ignoreCase = true) }
            ?: fail("invalid-player")

        player.incrementBoosters(booster, amount)

        sender.sendMessage(
            plugin.langYml.getMessage("gave-booster", StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
                .replace(oldValue = "%player%", newValue = player.name ?: playerName)
                .replace(oldValue = "%booster%", newValue = booster.name)
                .replace(oldValue = "%amount%", newValue = amount.toString())
        )
    }

    completes {
        when (args.size) {
            1 -> playerNames()
            2 -> Boosters.values().map { it.id.key }
            AMOUNT_ARGS -> amountCompletions
            else -> emptyList()
        }
    }
}
