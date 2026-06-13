package com.willfp.boosters.gui

import com.willfp.boosters.activateBooster
import com.willfp.boosters.boosters.ActivationResult
import com.willfp.boosters.boosters.Booster
import com.willfp.boosters.boosters.Boosters
import com.willfp.boosters.plugin
import com.willfp.eco.core.gui.addPage
import com.willfp.eco.core.gui.addPageChanger
import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.gui.page.PageChanger
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.ConfigSlot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.gui.slot.functional.SlotHandler
import com.willfp.eco.core.sound.PlayableSound
import com.willfp.eco.util.StringUtils
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
                    .replace("%duration%", booster.getFormattedTimeLeft(activationResult.duration.toInt() / 20))
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
            val pages = plugin.configYml.getSubsections("gui.pages")

            title = StringUtils.format(plugin.configYml.getString("gui.title"))

            maxPages(pages.size)

            val pageChangeSound = PlayableSound.create(
                plugin.configYml.getSubsection("gui.sound")
            )

            addPageChanger(plugin.configYml, "gui.forwards-arrow", PageChanger.Direction.FORWARDS, pageChangeSound)
            addPageChanger(plugin.configYml, "gui.backwards-arrow", PageChanger.Direction.BACKWARDS, pageChangeSound)

            for (pageConfig in pages) {
                val pageNumber = pageConfig.getInt("page")

                addPage(pageNumber) {
                    setMask(
                        FillerMask(
                            MaskItems.fromItemNames(pageConfig.getStrings("mask.items")),
                            *pageConfig.getStrings("mask.pattern").toTypedArray()
                        )
                    )

                    for (booster in Boosters.values()) {
                        if (booster.guiPage != pageNumber) {
                            continue
                        }

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

                    for (config in pageConfig.getSubsections("custom-slots")) {
                        setSlot(
                            config.getInt("row"),
                            config.getInt("column"),
                            ConfigSlot(config)
                        )
                    }
                }
            }
        }
    }

    fun open(player: Player) {
        gui.open(player)
    }
}