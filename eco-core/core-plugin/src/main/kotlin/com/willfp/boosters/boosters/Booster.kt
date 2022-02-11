package com.willfp.boosters.boosters

import com.willfp.boosters.BoostersPlugin
import com.willfp.boosters.getAmountOfBooster
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.formatEco
import com.willfp.libreforge.Holder
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.effects.Effects
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID

class Booster(
    plugin: BoostersPlugin,
    val config: Config,
) : Holder {
    val id = config.getString("id")

    val dataKey = PersistentDataKey(
        plugin.namespacedKeyFactory.create(id),
        PersistentDataKeyType.INT,
        0
    )

    val name = config.getFormattedString("name")

    val duration = config.getInt("duration")

    fun getActivationMessages(player: Player): List<String> {
        val messages = mutableListOf<String>()

        for (string in config.getFormattedStrings(
            "messages.activation",
            StringUtils.FormatOption.WITHOUT_PLACEHOLDERS
        )) {
            messages.add(string.replace("%player%", player.displayName))
        }

        return messages
    }

    val expiryMessages = config.getStrings("messages.expiry")

    fun getGuiItem(player: Player): ItemStack {
        return ItemStackBuilder(Items.lookup(config.getString("gui.item")))
            .setDisplayName(config.getFormattedString("gui.name"))
            .addLoreLines(
                config.getStrings("gui.lore")
                    .map { it.replace("%amount%", player.getAmountOfBooster(this).toString()) }
                    .formatEco(player)
            )
            .build()
    }

    val guiRow = config.getInt("gui.position.row")

    val guiColumn = config.getInt("gui.position.column")

    override val conditions = config.getSubsections("conditions").mapNotNull {
        Conditions.compile(it, "Booster $id")
    }.toSet()

    override val effects = config.getSubsections("effects").mapNotNull {
        Effects.compile(it, "Booster $id")
    }.toSet()

    init {
        Boosters.addNewBooster(this)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Booster) {
            return false
        }

        return other.id == this.id
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }
}

data class ActivatedBooster(
    val booster: Booster,
    val player: UUID
)
