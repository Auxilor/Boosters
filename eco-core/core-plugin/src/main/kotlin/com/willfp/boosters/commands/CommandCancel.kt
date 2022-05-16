package com.willfp.boosters.commands

import com.willfp.boosters.BoostersPlugin
import com.willfp.boosters.boosters.activeBoosters
import com.willfp.boosters.boosters.expireBooster
import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class CommandCancel(plugin: EcoPlugin) :
    Subcommand(
        plugin,
        "cancel",
        "boosters.command.cancel",
        false
    ) {

    override fun onExecute(sender: CommandSender, args: List<String>) {
        for (booster in Bukkit.getServer().activeBoosters) {
            Bukkit.getServer().expireBooster(booster.booster)
        }
        sender.sendMessage(plugin.langYml.getMessage("cancelled"))
    }
}
