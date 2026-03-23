package com.willfp.boosters.commands

import com.willfp.boosters.boosters.BoosterBossBar
import com.willfp.boosters.plugin
import com.willfp.boosters.toggleBossBarVisibility
import com.willfp.eco.core.command.impl.Subcommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object CommandBossbar : Subcommand(
    plugin,
    "bossbar",
    "boosters.command.bossbar",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        val player = sender as? Player

        if (player == null) {
            sender.sendMessage(plugin.langYml.getMessage("not-player"))
            return
        }

        val isVisible = player.toggleBossBarVisibility()
        BoosterBossBar.render()

        if (isVisible) {
            player.sendMessage(plugin.langYml.getMessage("bossbar-enabled"))
        } else {
            player.sendMessage(plugin.langYml.getMessage("bossbar-disabled"))
        }
    }
}

