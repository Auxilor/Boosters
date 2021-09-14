package com.willfp.boosters.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.CommandHandler
import com.willfp.eco.core.command.impl.Subcommand
import org.bukkit.command.CommandSender

class CommandReload(plugin: EcoPlugin) :
    Subcommand(
        plugin,
        "reload",
        "boosters.command.reload",
        false
    ) {
    override fun getHandler(): CommandHandler {
        return CommandHandler { sender: CommandSender, _: List<String> ->
            plugin.reload()
            sender.sendMessage(plugin.langYml.getMessage("reloaded"))
        }
    }
}