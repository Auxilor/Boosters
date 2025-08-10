package com.willfp.boosters.gui

import com.willfp.boosters.BoostersPlugin
import com.willfp.boosters.activateBooster
import com.willfp.boosters.boosters.Booster
import com.willfp.boosters.boosters.Boosters
import com.willfp.boosters.increaseBooster
import com.willfp.eco.core.config.updating.ConfigUpdater
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.gui.slot.functional.SlotHandler
import com.willfp.eco.util.tryAsPlayer
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player

object BoosterGUI {
    private lateinit var gui: Menu

    private fun makeHandler(booster: Booster, plugin: BoostersPlugin): SlotHandler {
        return SlotHandler { event, _, _ ->
            val player = event.whoClicked.tryAsPlayer() ?: return@SlotHandler

            if (booster.active != null) {
                if (!player.increaseBooster(booster)) {
                    player.sendMessage(plugin.langYml.getMessage("dont-have"))
                    player.playSound(
                        player.location,
                        Sound.BLOCK_NOTE_BLOCK_BASS,
                        1f,
                        0.5f
                    )
                }
                return@SlotHandler
            }

            if (!player.activateBooster(booster)) {
                player.sendMessage(plugin.langYml.getMessage("dont-have"))
                player.playSound(
                    player.location,
                    Sound.BLOCK_NOTE_BLOCK_BASS,
                    1f,
                    0.5f
                )
                return@SlotHandler
            }

            player.closeInventory()
        }
    }

    internal fun update(plugin: BoostersPlugin) {
        gui = menu(plugin.configYml.getInt("gui.rows")) {
            setMask(
                FillerMask(
                    MaskItems.fromItemNames(plugin.configYml.getStrings("gui.mask.items")),
                    *plugin.configYml.getStrings("gui.mask.pattern").toTypedArray()
                )
            )

            for (booster in Boosters.values()) {
                setSlot(
                    booster.guiRow,
                    booster.guiColumn,
                    slot(
                        { player, _ -> booster.getGuiItem(player) }
                    ) {
                        onLeftClick(makeHandler(booster, plugin))
                    }
                )
            }

            setTitle(plugin.configYml.getFormattedString("gui.title"))
        }
    }

    fun open(player: Player) {
        gui.open(player)
    }
}
