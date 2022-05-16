package com.willfp.boosters.commands

import com.willfp.boosters.gui.BoosterGUI
import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandBoosters(plugin: EcoPlugin) :
    PluginCommand(
        plugin,
        "boosters",
        "boosters.command.boosters",
        false
    ) {

    init {
        this.addSubcommand(CommandGive(plugin))
            .addSubcommand(CommandReload(plugin))
            .addSubcommand(CommandCancel(plugin))
            .addSubcommand(CommandActivate(plugin))
    }

    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (sender !is Player) {
            sender.sendMessage(this.plugin.langYml.getMessage("not-player"))
            return
        }

        BoosterGUI.open(sender)
    }
}
