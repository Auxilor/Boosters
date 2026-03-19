package com.willfp.boosters.commands

import com.willfp.boosters.gui.BoosterGUI
import com.willfp.boosters.plugin
import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object CommandBoosters : PluginCommand(
    plugin,
    "boosters",
    "boosters.command.boosters",
    false
) {

    init {
        this.addSubcommand(CommandGive)
            .addSubcommand(CommandReload)
            .addSubcommand(CommandCancel)
            .addSubcommand(CommandActivate)
            .addSubcommand(CommandBossbar)
            .addSubcommand(CommandQueue)
    }

    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (sender !is Player) {
            sender.sendMessage(this.plugin.langYml.getMessage("not-player"))
            return
        }

        BoosterGUI.open(sender)
    }
}
