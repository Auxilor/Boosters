package com.willfp.boosters.gui

import com.willfp.boosters.BoostersPlugin
import com.willfp.boosters.activateBooster
import com.willfp.boosters.activeBooster
import com.willfp.boosters.boosters.Booster
import com.willfp.boosters.boosters.Boosters
import com.willfp.eco.core.config.updating.ConfigUpdater
import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.gui.slot.functional.SlotHandler
import com.willfp.libreforge.tryAsPlayer
import org.bukkit.Sound
import org.bukkit.entity.Player

object BoosterGUI {
    private lateinit var gui: Menu

    private fun makeHandler(booster: Booster, plugin: BoostersPlugin): SlotHandler {
        return SlotHandler { event, _, _ ->
            val player = event.whoClicked.tryAsPlayer() ?: return@SlotHandler

            if (activeBooster != null) {
                player.sendMessage(plugin.langYml.getMessage("already-active"))
                player.playSound(
                    player.location,
                    Sound.BLOCK_NOTE_BLOCK_BASS,
                    1f,
                    0.5f
                )
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

    @JvmStatic
    @ConfigUpdater
    fun update(plugin: BoostersPlugin) {
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
                        setUpdater { player, _, prev ->
                            val newItem = booster.getGuiItem(player)
                            prev.itemMeta = newItem.itemMeta
                            prev.type = newItem.type
                            prev.itemMeta?.lore?.forEach { println(it) }
                            prev
                        }
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
