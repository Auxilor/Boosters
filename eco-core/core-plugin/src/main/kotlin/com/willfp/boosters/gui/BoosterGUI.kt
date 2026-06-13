package com.willfp.boosters.gui

import com.willfp.boosters.activateBooster
import com.willfp.boosters.boosters.ActivationResult
import com.willfp.boosters.boosters.Booster
import com.willfp.boosters.boosters.Boosters
import com.willfp.boosters.plugin
import com.willfp.eco.core.gui.GUIComponent
import com.willfp.eco.core.gui.addPage
import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.gui.menu.MenuLayer
import com.willfp.eco.core.gui.page.Page
import com.willfp.eco.core.gui.page.PageChanger
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.ConfigSlot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.gui.slot.Slot
import com.willfp.eco.core.gui.slot.functional.SlotHandler
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.sound.PlayableSound
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.tryAsPlayer
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

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

            val maxPage = pages.size.coerceAtLeast(1)

            val formattedTitle = StringUtils.format(plugin.configYml.getString("gui.title"))

            title = formattedTitle.withPagePlaceholders(1, maxPage)

            onRender { player, menu ->
                menu.refreshPageTitle(player, formattedTitle, maxPage)
            }

            maxPages(pages.size)

            val pageChangeSound = PlayableSound.create(
                plugin.configYml.getSubsection("gui.sound")
            )

            for (direction in PageChanger.Direction.entries) {
                val arrowKey = "${direction.name.lowercase()}-arrow"

                addComponent(
                    MenuLayer.TOP,
                    plugin.configYml.getInt("gui.$arrowKey.row"),
                    plugin.configYml.getInt("gui.$arrowKey.column"),
                    PageChangerComponent(direction, pageChangeSound) { state, page, max ->
                        val key = if (state == PageButtonState.ACTIVE) "item" else "item-inactive"
                        plugin.configYml.getStringOrNull("gui.$arrowKey.$key")
                            ?.let { Items.lookup(it.withPagePlaceholders(page, max)).item }
                    }
                )
            }

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

enum class PageButtonState { ACTIVE, INACTIVE }

fun String.withPagePlaceholders(page: Int, maxPage: Int): String =
    this.replace("%page%", page.toString())
        .replace("%max_page%", maxPage.toString())

class PageChangerComponent(
    private val direction: PageChanger.Direction,
    private val sound: PlayableSound?,
    private val itemProvider: (state: PageButtonState, page: Int, maxPage: Int) -> ItemStack?
) : GUIComponent {
    override fun getRows() = 1
    override fun getColumns() = 1

    override fun getSlotAt(row: Int, column: Int, player: Player, menu: Menu): Slot? {
        val page = menu.getPage(player)
        val maxPage = menu.getMaxPage(player)

        val isInactive = (page <= 1 && direction == PageChanger.Direction.BACKWARDS)
                || (page >= maxPage && direction == PageChanger.Direction.FORWARDS)

        if (isInactive) {
            val item = itemProvider(PageButtonState.INACTIVE, page, maxPage) ?: return null
            return slot(item)
        }

        val item = itemProvider(PageButtonState.ACTIVE, page, maxPage) ?: return null
        return slot(item) {
            onLeftClick { event, _, clickedMenu ->
                val clicker = event.whoClicked as Player
                val current = clickedMenu.getPage(clicker)
                val newPage = (current + direction.change)
                    .coerceIn(1, clickedMenu.getMaxPage(clicker))

                if (newPage == current) {
                    return@onLeftClick
                }

                clickedMenu.setState(clicker, Page.PAGE_KEY, newPage)
                sound?.playTo(clicker)
            }
        }
    }
}

fun Menu.refreshPageTitle(player: Player, rawTitle: String, maxPage: Int) {
    val title = rawTitle.withPagePlaceholders(this.getPage(player), maxPage)

    if (this.getState<String>(player, "pagination.shownTitle") == title) {
        return
    }

    if (!player.openInventory.topInventory.type.isCreatable) {
        return
    }

    @Suppress("DEPRECATION")
    player.openInventory.setTitle(title)
    this.setState(player, "pagination.shownTitle", title)
}