package com.willfp.boosters.boosters

import com.willfp.boosters.BoostersPlugin
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.util.StringUtils
import org.bukkit.entity.Player
import org.bukkit.event.Listener

abstract class Booster(
    private val plugin: BoostersPlugin,
    val id: String
): Listener {
    abstract val duration: Int

    val dataKey = PersistentDataKey<Int>(
        plugin.namespacedKeyFactory.create(id),
        PersistentDataKeyType.INT,
        0
    )

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

    fun getActivationMessages(player: Player): List<String> {
        val messages = mutableListOf<String>()

        for (string in this.plugin.configYml.getStrings(
            "messages.${this.id}.activation",
            true,
            StringUtils.FormatOption.WITHOUT_PLACEHOLDERS
        )) {
            messages.add(string.replace("%player%", player.displayName))
        }

        return messages
    }

    fun getExpiryMessages(): List<String> {
        return this.plugin.configYml.getStrings("messages.${this.id}.expiry")
    }
}