package com.willfp.boosters.commands

import com.willfp.boosters.activateBooster
import com.willfp.boosters.activateBoosterConsole
import com.willfp.boosters.boosters.Boosters
import com.willfp.boosters.increaseBooster
import com.willfp.boosters.incrementBoosterConsole
import com.willfp.boosters.plugin
import com.willfp.eco.core.command.impl.Subcommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

object CommandActivate : Subcommand(
    plugin,
    "activate",
    "boosters.command.activate",
    false
) {

    override fun onExecute(sender: CommandSender, args: List<String>) {

        if (args.isEmpty()) {
            sender.sendMessage(plugin.langYml.getMessage("requires-booster"))
            return
        }

        val booster = Boosters.getByID(args[0].lowercase())

        if (booster == null) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-booster"))
            return
        }

        val player = sender as? Player

        if (player == null) {
            if (booster.active != null) {
                Bukkit.getServer().incrementBoosterConsole(booster)
            } else {
                Bukkit.getServer().activateBoosterConsole(booster)
            }
            return
        }

        if (booster.active != null) {
            player.increaseBooster(booster)
        } else {
            player.activateBooster(booster)
        }
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
