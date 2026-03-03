package com.willfp.boosters.commands

import com.willfp.boosters.boosters.BoosterQueue
import com.willfp.boosters.boosters.Boosters
import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.formatEco
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

class CommandQueue(plugin: EcoPlugin) :
    Subcommand(
        plugin,
        "queue",
        "boosters.command.queue",
        false
    ) {

    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.langYml.getMessage("requires-category"))
            return
        }

        val category = args[0].lowercase()

        val queued = BoosterQueue.queue[category] ?: emptyList()

        val format = if (queued.isEmpty()) {
            plugin.langYml.getStrings("empty-queue-format")
        } else {
            plugin.langYml.getStrings("queue-format")
        }

        val resultingMessage = mutableListOf<String>()

        for (s in format) {
            if (s.contains("%number%")) {
                var i = 1
                for (booster in queued) {
                    resultingMessage.add(
                        s.replace("%number%", i.toString())
                            .replace("%booster%", booster.booster.name)
                            .replace("%time%", booster.booster
                                .getFormattedTimeLeft(booster.duration / 20))
                            .replace("%activator%", booster.getActivatorName())
                            .replace("%category%", category)
                            .formatEco(sender as? Player, true)
                    )
                    i++
                }
            } else {
                resultingMessage.add(s.replace("%category%", category)
                    .formatEco(sender as? Player, true))
            }
        }

        for (s in resultingMessage) {
            sender.sendMessage(s)
        }
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        val completions = mutableListOf<String>()

        if (args.size == 1) {
            StringUtil.copyPartialMatches(
                args[0],
                Boosters.getCategories(),
                completions
            )
            return completions
        }

        return emptyList()
    }
}
