package com.willfp.boosters.commands

import com.willfp.boosters.bossBarManager
import com.willfp.boosters.toggleBossBarVisibility
import org.bukkit.command.CommandSender
import wiki.ecohub.core.command.CommandBuilder

internal fun CommandBuilder<CommandSender>.bossbarCommand() = playerSubcommand(
    name = "bossbar",
    permission = "boosters.command.bossbar"
) {
    executes {
        val isVisible = sender.toggleBossBarVisibility()
        bossBarManager.render()

        if (isVisible) {
            reply("bossbar-enabled")
        } else {
            reply("bossbar-disabled")
        }
    }
}
