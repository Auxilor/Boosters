package com.willfp.boosters.commands

import com.willfp.boosters.plugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.toNiceString
import org.bukkit.command.CommandSender

object CommandReload : Subcommand(
    plugin,
    "reload",
    "boosters.command.reload",
    false
) {

    override fun onExecute(sender: CommandSender, args: List<String>) {
        sender.sendMessage(
            plugin.langYml.getMessage("reloaded", StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
                .replace("%time%", plugin.reloadWithTime().toNiceString())
        )
    }
}