package com.willfp.boosters.commands

import com.willfp.boosters.BoostersPlugin
import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import org.bukkit.command.CommandSender

class CommandCancel(plugin: EcoPlugin) :
    Subcommand(
        plugin,
        "cancel",
        "boosters.command.cancel",
        false
    ) {

    override fun onExecute(sender: CommandSender, args: List<String>) {
        (plugin as BoostersPlugin).activeBooster = null
        sender.sendMessage(plugin.langYml.getMessage("cancelled"))
    }
}
