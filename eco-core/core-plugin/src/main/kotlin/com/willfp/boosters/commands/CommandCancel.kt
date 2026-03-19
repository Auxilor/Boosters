package com.willfp.boosters.commands

import com.willfp.boosters.boosters.BoosterQueue
import com.willfp.boosters.boosters.activeBoosters
import com.willfp.boosters.boosters.expireBooster
import com.willfp.boosters.plugin
import com.willfp.boosters.runExpiryEffects
import com.willfp.eco.core.command.impl.Subcommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

object CommandCancel : Subcommand(
    plugin,
    "cancel",
    "boosters.command.cancel",
    false
) {

    override fun onExecute(sender: CommandSender, args: List<String>) {
        val silent = args.firstOrNull()?.equals("silent", true) == true

        for (booster in Bukkit.getServer().activeBoosters) {
            if (!silent) {
                booster.booster.runExpiryEffects()
            }

            Bukkit.getServer().expireBooster(booster.booster)

            BoosterQueue.queue.clear()

            BoosterQueue.saveQueue()
        }

        sender.sendMessage(plugin.langYml.getMessage("cancelled"))
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        val completions = mutableListOf<String>()

        if (args.size == 1) {
            StringUtil.copyPartialMatches(args[0], listOf("silent"), completions)
            return completions
        }

        return emptyList()
    }
}
