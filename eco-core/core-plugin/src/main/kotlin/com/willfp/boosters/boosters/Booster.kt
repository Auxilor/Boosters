package com.willfp.boosters.boosters

import com.willfp.boosters.BoostersPlugin
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.util.StringUtils
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import java.util.UUID

abstract class Booster(
    private val plugin: BoostersPlugin,
    val id: String
) : Listener {
    abstract val duration: Int

    val dataKey = PersistentDataKey(
        plugin.namespacedKeyFactory.create(id),
        PersistentDataKeyType.INT,
        0
    )

    val name = plugin.configYml.getFormattedString("messages.${this.id}.name")

    init {
        register()
    }

    private fun register() {
        Boosters.registerNewBooster(this)
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

    fun getActivationMessages(player: Player): List<String> {
        val messages = mutableListOf<String>()

        for (string in this.plugin.configYml.getFormattedStrings(
            "messages.${this.id}.activation",
            StringUtils.FormatOption.WITHOUT_PLACEHOLDERS
        )) {
            messages.add(string.replace("%player%", player.displayName))
        }

        return messages
    }

    fun getExpiryMessages(): List<String> {
        return this.plugin.configYml.getFormattedStrings("messages.${this.id}.expiry")
    }
}

data class ActivatedBooster(
    val booster: Booster,
    val player: UUID
)
