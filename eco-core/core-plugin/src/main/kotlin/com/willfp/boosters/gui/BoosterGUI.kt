package com.willfp.boosters.gui

import com.willfp.boosters.BoostersPlugin
import com.willfp.boosters.activateBooster
import com.willfp.boosters.boosters.ActivationResult
import com.willfp.boosters.boosters.Booster
import com.willfp.boosters.boosters.Boosters
import com.willfp.boosters.increaseBooster
import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.ConfigSlot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.gui.slot.functional.SlotHandler
import com.willfp.eco.util.tryAsPlayer
import org.bukkit.Sound
import org.bukkit.entity.Player

object BoosterGUI {
    private lateinit var gui: Menu

    private fun makeHandler(booster: Booster): SlotHandler {
        return SlotHandler { event, _, _ ->
            val player = event.whoClicked.tryAsPlayer() ?: return@SlotHandler

            val activationResult = player.activateBooster(booster)

            player.sendMessage(
                plugin.langYml.getMessage(activationResult.result.langString)
                    .replace("%booster%", booster.name)
                    .replace("%duration%", booster.getFormattedTimeLeft())
            )

            if (activationResult.result in listOf(ActivationResult.DENIED_CONDITIONS,
                    ActivationResult.INSUFFICIENT_AMOUNT
                )) {
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

    internal fun update() {
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
                        onLeftClick(makeHandler(booster))
                    }
                )
            }

            for (config in plugin.configYml.getSubsections("gui.custom-slots")) {
                setSlot(
                    config.getInt("row"),
                    config.getInt("column"),
                    ConfigSlot(config)
                )
            }

            title = plugin.configYml.getFormattedString("gui.title")
        }
    }

    fun open(player: Player) {
        gui.open(player)
    }
}
