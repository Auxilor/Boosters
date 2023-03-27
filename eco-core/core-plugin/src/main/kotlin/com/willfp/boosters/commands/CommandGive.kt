package com.willfp.boosters.commands

import com.willfp.boosters.boosters.Boosters
import com.willfp.boosters.incrementBoosters
import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.StringUtils
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

class CommandGive(plugin: EcoPlugin) :
    Subcommand(
        plugin,
        "give",
        "boosters.command.give",
        false
    ) {

    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.langYml.getMessage("requires-player"))
            return
        }

        if (args.size == 1) {
            sender.sendMessage(plugin.langYml.getMessage("requires-booster"))
            return
        }

        val booster = Boosters.getByID(args[1].lowercase())

        if (booster == null) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-booster"))
            return
        }

        var amount = 1

        if (args.size >= 3) {
            amount = args[2].toIntOrNull() ?: amount
        }

        @Suppress("DEPRECATION")
        val player = Bukkit.getOfflinePlayer(args[0])
        if (!player.hasPlayedBefore()) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-player"))
            return
        }

        player.incrementBoosters(booster, amount)

        sender.sendMessage(
            plugin.langYml.getMessage("gave-booster", StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
                .replace("%player%", player.name ?: return)
                .replace("%booster%", booster.name)
                .replace("%amount%", amount.toString())
        )
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        val completions = mutableListOf<String>()

        if (args.size == 1) {
            StringUtil.copyPartialMatches(
                args[0],
                Bukkit.getOnlinePlayers().map { player -> player.name }.toCollection(ArrayList()),
                completions
            )
            return completions
        }

        if (args.size == 2) {
            StringUtil.copyPartialMatches(
                args[1],
                Boosters.values().map { it.id.key },
                completions
            )
            return completions
        }

        if (args.size == 3) {
            StringUtil.copyPartialMatches(
                args[2],
                listOf(
                    "1",
                    "2",
                    "3",
                    "4",
                    "5",
                    "10"
                ),
                completions
            )
            return completions
        }

        return emptyList()
    }
}
