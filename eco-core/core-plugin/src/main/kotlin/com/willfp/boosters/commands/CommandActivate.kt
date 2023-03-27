package com.willfp.boosters.commands

import com.willfp.boosters.activateBooster
import com.willfp.boosters.boosters.Boosters
import com.willfp.boosters.incrementBoosters
import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

class CommandActivate(plugin: EcoPlugin) :
    Subcommand(
        plugin,
        "activate",
        "boosters.command.activate",
        true
    ) {

    override fun onExecute(sender: CommandSender, args: List<String>) {
        val player = sender as? Player ?: return

        if (args.isEmpty()) {
            sender.sendMessage(plugin.langYml.getMessage("requires-booster"))
            return
        }

        val booster = Boosters.getByID(args[0].lowercase())

        if (booster == null) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-booster"))
            return
        }

        player.incrementBoosters(booster, 1)
        player.activateBooster(booster)
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        val completions = mutableListOf<String>()

        if (args.size == 1) {
            StringUtil.copyPartialMatches(
                args[0],
                Boosters.values().map { it.id.key },
                completions
            )
            return completions
        }

        return emptyList()
    }
}
