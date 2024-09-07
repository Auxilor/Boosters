package com.willfp.boosters.commands

import com.willfp.boosters.activateBooster
import com.willfp.boosters.boosters.ActivatedBooster
import com.willfp.boosters.boosters.Booster
import com.willfp.boosters.boosters.Boosters
import com.willfp.boosters.boosters.activateBooster
import com.willfp.boosters.incrementBoosters
import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.formatEco
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

class CommandActivate(plugin: EcoPlugin) :
    Subcommand(
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
            activateBoosterConsole(booster)
            return
        }

        player.incrementBoosters(booster, 1)
        player.activateBooster(booster)
    }

    private fun activateBoosterConsole(booster: Booster) {

        for (activationCommand in booster.activationCommands) {
            Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                activationCommand.replace("%player%", plugin.langYml.getMessage("console-displayname").formatEco(formatPlaceholders = false))
            )
        }

        for (activationMessage in booster.getActivationMessages(null)) {
            @Suppress("DEPRECATION")
            Bukkit.broadcastMessage(activationMessage)
        }

        Bukkit.getServer().activateBooster(
            ActivatedBooster(booster, null)
        )

        for (player in Bukkit.getOnlinePlayers()) {
            player.playSound(
                player.location,
                Sound.UI_TOAST_CHALLENGE_COMPLETE,
                2f,
                0.9f
            )
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
