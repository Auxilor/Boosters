package com.willfp.boosters.commands

import com.willfp.boosters.BoostersPlugin
import com.willfp.boosters.gui.BoosterGUI
import org.bukkit.entity.Player
import wiki.ecohub.core.command.command

internal fun BoostersPlugin.boostersCommand() = command(name = "boosters", permission = "boosters.command.boosters") {
    aliases("boost")

    giveCommand()
    reloadCommand()
    cancelCommand()
    activateCommand()
    bossbarCommand()
    queueCommand()

    executes {
        BoosterGUI.open(sender as? Player ?: fail("not-player"))
    }
}
