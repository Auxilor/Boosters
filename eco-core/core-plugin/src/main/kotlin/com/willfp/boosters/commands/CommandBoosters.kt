package com.willfp.boosters.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.CommandHandler
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.boosters.gui.SkillGUI
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandBoosters(plugin: EcoPlugin) :
    PluginCommand(
        plugin,
        "boosters",
        "boosters.command.boosters",
        true
    ) {

    init {
        this.addSubcommand(CommandGive(plugin))
            .addSubcommand(CommandReload(plugin))
    }

    override fun getHandler(): CommandHandler {
        return CommandHandler { sender: CommandSender, _: List<String> ->
            if (sender !is Player) {
                sender.sendMessage(this.plugin.langYml.getMessage("not-player"))
                return@CommandHandler
            }

            SkillGUI.getHomeMenu().open(sender)
        }
    }
}