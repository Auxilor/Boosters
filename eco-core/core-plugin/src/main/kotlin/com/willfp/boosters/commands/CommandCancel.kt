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
        if (args.isEmpty()) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-command"))
            return
        }

        when (args[0].lowercase()) {
            "all" -> {
                val silent = args.getOrNull(1)?.equals("silent", true) == true

                for (booster in Bukkit.getServer().activeBoosters) {
                    if (!silent) {
                        booster.booster.runExpiryEffects()
                    }

                    Bukkit.getServer().expireBooster(booster.booster)
                }

                BoosterQueue.queue.clear()
                BoosterQueue.saveQueue()

                sender.sendMessage(plugin.langYml.getMessage("cancelled"))
            }

            "booster" -> {
                if (args.size < 2) {
                    sender.sendMessage(plugin.langYml.getMessage("requires-booster"))
                    return
                }

                val boosterId = args[1].lowercase()
                val silent = args.getOrNull(2)?.equals("silent", true) == true

                val target = Bukkit.getServer().activeBoosters.firstOrNull {
                    it.booster.id.key.equals(boosterId, true)
                }

                if (target == null) {
                    sender.sendMessage(plugin.langYml.getMessage("invalid-active-booster"))
                    return
                }

                if (!silent) {
                    target.booster.runExpiryEffects()
                }

                Bukkit.getServer().expireBooster(target.booster)
                target.booster.category?.let { BoosterQueue.queue.remove(it) }
                BoosterQueue.saveQueue()

                sender.sendMessage(plugin.langYml.getMessage("cancelled"))
            }

            "category" -> {
                if (args.size < 2) {
                    sender.sendMessage(plugin.langYml.getMessage("requires-category"))
                    return
                }

                val categoryId = args[1].lowercase()
                val silent = args.getOrNull(2)?.equals("silent", true) == true

                val targets = Bukkit.getServer().activeBoosters.filter {
                    it.booster.category.equals(categoryId, true)
                }

                if (targets.isEmpty()) {
                    sender.sendMessage(plugin.langYml.getMessage("invalid-active-category"))
                    return
                }

                for (target in targets) {
                    if (!silent) {
                        target.booster.runExpiryEffects()
                    }

                    Bukkit.getServer().expireBooster(target.booster)
                }

                BoosterQueue.queue.remove(categoryId)
                BoosterQueue.saveQueue()

                sender.sendMessage(plugin.langYml.getMessage("cancelled"))
            }

            else -> sender.sendMessage(plugin.langYml.getMessage("invalid-command"))
        }
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        val completions = mutableListOf<String>()

        if (args.size == 1) {
            StringUtil.copyPartialMatches(args[0], listOf("all", "booster", "category"), completions)
            return completions
        }

        if (args.size == 2) {
            val options = when (args[0].lowercase()) {
                "all" -> listOf("silent")
                "booster" -> Bukkit.getServer().activeBoosters.map { it.booster.id.key }
                "category" -> Bukkit.getServer().activeBoosters.mapNotNull { it.booster.category }.distinct()
                else -> emptyList()
            }

            StringUtil.copyPartialMatches(args[1], options, completions)
            return completions
        }

        if (args.size == 3) {
            when (args[0].lowercase()) {
                "booster", "category" -> {
                    StringUtil.copyPartialMatches(args[2], listOf("silent"), completions)
                    return completions
                }
            }
        }

        return emptyList()
    }
}
