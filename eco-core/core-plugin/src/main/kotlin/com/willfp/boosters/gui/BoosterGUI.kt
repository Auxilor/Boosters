package com.willfp.boosters.gui

import com.willfp.boosters.BoostersPlugin
import com.willfp.boosters.activateBooster
import com.willfp.boosters.activeBooster
import com.willfp.boosters.boosters.Booster
import com.willfp.boosters.boosters.Boosters
import com.willfp.boosters.getAmountOfBooster
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.Slot
import com.willfp.eco.core.gui.slot.functional.SlotHandler
import com.willfp.eco.core.items.builder.SkullBuilder
import com.willfp.eco.util.StringUtils
import com.willfp.ecoskills.tryAsPlayer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player

object BoosterGUI {
    private val plugin = BoostersPlugin.instance

    private fun makeHandler(booster: Booster): SlotHandler {
        return SlotHandler { event, _, _ ->
            val player = event.whoClicked.tryAsPlayer() ?: return@SlotHandler

            if (Bukkit.getServer().activeBooster != null) {
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

    private val gui = Menu.builder(3)
        .setMask(
            FillerMask(
                Material.BLACK_STAINED_GLASS_PANE,
                "111111111",
                "101101101",
                "111111111"
            )
        )
        .setSlot(
            2,
            2,
            Slot.builder(
                SkullBuilder()
                    .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTM0YjI3YmZjYzhmOWI5NjQ1OTRiNjE4YjExNDZhZjY5ZGUyNzhjZTVlMmUzMDEyY2I0NzFhOWEzY2YzODcxIn19fQ==")
                    .build()
            )
                .setModifier { player, _, previous ->
                    val meta = previous.itemMeta ?: return@setModifier
                    val lore = mutableListOf<String>()

                    lore.add("")
                    lore.add("&fGives everyone online a")
                    lore.add("&a1.5x Sell Multiplier")
                    lore.add("&fto make money faster!")
                    lore.add("")
                    lore.add("&fDuration: &a1 Hour")
                    lore.add("")
                    lore.add("&fYou have: &a${player.getAmountOfBooster(Boosters.SELL_MULTIPLIER_LOW)}")
                    lore.add("&fGet more at &astore.ecomc.net")
                    lore.add("")
                    lore.add("&e&oClick to activate!")
                    lore.add("")

                    meta.setDisplayName(StringUtils.format("&d1.5x Sell Multiplier"))

                    meta.lore = lore.apply {
                        replaceAll { StringUtils.format(it) }
                    }
                    previous.itemMeta = meta

                }
                .onLeftClick(makeHandler(Boosters.SELL_MULTIPLIER_LOW))
                .build()
        )
        .setSlot(
            2,
            5,
            Slot.builder(
                SkullBuilder()
                    .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjBhN2I5NGM0ZTU4MWI2OTkxNTlkNDg4NDZlYzA5MTM5MjUwNjIzN2M4OWE5N2M5MzI0OGEwZDhhYmM5MTZkNSJ9fX0=")
                    .build()
            )
                .setModifier { player, _, previous ->
                    val meta = previous.itemMeta ?: return@setModifier
                    val lore = mutableListOf<String>()

                    lore.add("")
                    lore.add("&fGives everyone online a")
                    lore.add("&a2x Sell Multiplier")
                    lore.add("&fto make money faster!")
                    lore.add("")
                    lore.add("&fDuration: &a1 Hour")
                    lore.add("")
                    lore.add("&fYou have: &a${player.getAmountOfBooster(Boosters.SELL_MULTIPLIER_HIGH)}")
                    lore.add("&fGet more at &astore.ecomc.net")
                    lore.add("")
                    lore.add("&e&oClick to activate!")
                    lore.add("")

                    meta.setDisplayName(StringUtils.format("&d2x Sell Multiplier"))

                    meta.lore = lore.apply {
                        replaceAll { StringUtils.format(it) }
                    }
                    previous.itemMeta = meta

                }
                .onLeftClick(makeHandler(Boosters.SELL_MULTIPLIER_HIGH))
                .build()
        )
        .setSlot(
            2,
            8,
            Slot.builder(
                SkullBuilder()
                    .setSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODkyNmMxZjJjM2MxNGQwODZjNDBjZmMyMzVmZTkzODY5NGY0YTUxMDY3YWRhNDcyNmI0ODZlYTFjODdiMDNlMiJ9fX0=")
                    .build()
            )
                .setModifier { player, _, previous ->
                    val meta = previous.itemMeta ?: return@setModifier
                    val lore = mutableListOf<String>()

                    lore.add("")
                    lore.add("&fGives everyone online a")
                    lore.add("&a2x Skill XP Multiplier")
                    lore.add("&fto level up faster!")
                    lore.add("")
                    lore.add("&fDuration: &a1 Hour")
                    lore.add("")
                    lore.add("&fYou have: &a${player.getAmountOfBooster(Boosters.SKILL_XP)}")
                    lore.add("&fGet more at &astore.ecomc.net")
                    lore.add("")
                    lore.add("&e&oClick to activate!")
                    lore.add("")

                    meta.setDisplayName(StringUtils.format("&d2x Skill XP Multiplier"))

                    meta.lore = lore.apply {
                        replaceAll { StringUtils.format(it) }
                    }
                    previous.itemMeta = meta

                }
                .onLeftClick(makeHandler(Boosters.SKILL_XP))
                .build()
        )
        .setTitle("Boosters")
        .build()

    fun open(player: Player) {
        gui.open(player)
    }
}