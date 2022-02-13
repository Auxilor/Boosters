package com.willfp.boosters.commands

import com.willfp.boosters.BoosterUtils
import com.willfp.boosters.gui.BoosterGUI
import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.core.command.impl.Subcommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandCancel(plugin: EcoPlugin) :
    Subcommand(
        plugin,
        "cancel",
        "boosters.command.cancel",
        false
    ) {

    override fun onExecute(sender: CommandSender, args: List<String>) {
        BoosterUtils.setActiveBooster(null)
        sender.sendMessage(plugin.langYml.getMessage("cancelled"))
    }
}
