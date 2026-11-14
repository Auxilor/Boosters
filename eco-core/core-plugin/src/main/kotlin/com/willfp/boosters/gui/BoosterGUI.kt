package com.willfp.boosters.gui

import com.willfp.boosters.activateBooster
import com.willfp.boosters.boosters.ActivationResult
import com.willfp.boosters.boosters.Booster
import com.willfp.boosters.boosters.Boosters
import com.willfp.boosters.plugin
import org.bukkit.Sound
import org.bukkit.entity.Player
import wiki.ecohub.game.entity.player.PlayerUtils
import wiki.ecohub.game.sound.PlayableSound
import wiki.ecohub.modules.menus.addCustomSlots
import wiki.ecohub.modules.menus.addPage
import wiki.ecohub.modules.menus.addPageChanger
import wiki.ecohub.modules.menus.menu
import wiki.ecohub.modules.menus.menu.Menu
import wiki.ecohub.modules.menus.page.PageChanger
import wiki.ecohub.modules.menus.setMask
import wiki.ecohub.modules.menus.slot
import wiki.ecohub.modules.menus.slot.SlotHandler
import wiki.ecohub.ui.text.StringUtils

object BoosterGUI {
    @Volatile
    private lateinit var gui: Menu

    private fun makeHandler(booster: Booster): SlotHandler {
        return SlotHandler { event, _, _ ->
            val player = PlayerUtils.tryAsPlayer(event.whoClicked) ?: return@SlotHandler

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
        val rows = plugin.configYml.getInt("gui.rows")

        gui = menu(rows) {
            val maxPage = Boosters.values().maxOfOrNull { it.guiPage } ?: 1

            setTitle(StringUtils.format(plugin.configYml.getString("gui.title")))

            maxPages(maxPage)

            val pageChangeSound = PlayableSound.create(
                plugin.configYml.getSubsection("gui.sound")
            )

            addPageChanger(plugin.configYml, "gui.forwards-arrow", PageChanger.Direction.FORWARDS, pageChangeSound)
            addPageChanger(plugin.configYml, "gui.backwards-arrow", PageChanger.Direction.BACKWARDS, pageChangeSound)

            for (pageNumber in 1..maxPage) {
                addPage(pageNumber) {
                    setMask(plugin.configYml, "gui")

                    for (booster in Boosters.values()) {
                        if (booster.guiPage != pageNumber) {
                            continue
                        }

                        // One booster with no position would otherwise stop the whole menu from building.
                        if (booster.guiRow !in 1..rows || booster.guiColumn !in 1..9) {
                            plugin.logger.warning(
                                "Booster ${booster.id.key} has no valid gui.position (row ${booster.guiRow}, column ${booster.guiColumn}), so it is left out of the GUI"
                            )
                            continue
                        }

                        setSlot(
                            row = booster.guiRow,
                            column = booster.guiColumn,
                            slot = slot(
                                { player, _ -> booster.getGuiItem(player) }
                            ) {
                                onLeftClick(makeHandler(booster))
                            }
                        )
                    }

                    addCustomSlots(plugin.configYml, "gui")
                }
            }
        }
    }

    fun open(player: Player) {
        if (!::gui.isInitialized) {
            plugin.logger.warning("Tried to open the boosters GUI for ${player.name} before it finished loading")
            return
        }

        gui.open(player)
    }
}