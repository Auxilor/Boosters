package com.willfp.boosters.commands

import com.willfp.boosters.boosters.Boosters
import com.willfp.boosters.gui.BoosterGUI
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.libreforge.LibReforgePlugin
import com.willfp.libreforge.lrcdb.CommandExport
import com.willfp.libreforge.lrcdb.CommandImport
import com.willfp.libreforge.lrcdb.ExportableConfig
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandBoosters(plugin: LibReforgePlugin) :
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
            .addSubcommand(CommandImport("boosters", plugin))
            .addSubcommand(CommandExport(plugin) {
                Boosters.values().map {
                    ExportableConfig(
                        it.id,
                        it.config
                    )
                }
            })
    }

    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (sender !is Player) {
            sender.sendMessage(this.plugin.langYml.getMessage("not-player"))
            return
        }

        BoosterGUI.open(sender)
    }
}
