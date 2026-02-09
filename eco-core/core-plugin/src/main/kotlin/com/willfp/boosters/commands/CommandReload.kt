package com.willfp.boosters.commands

import com.willfp.boosters.plugin
import com.willfp.eco.core.command.impl.Subcommand
import org.bukkit.command.CommandSender

object CommandReload : Subcommand(
    plugin,
    "reload",
    "boosters.command.reload",
    false
) {

    override fun onExecute(sender: CommandSender, args: List<String>) {
        plugin.reload()
        sender.sendMessage(plugin.langYml.getMessage("reloaded"))
    }
}