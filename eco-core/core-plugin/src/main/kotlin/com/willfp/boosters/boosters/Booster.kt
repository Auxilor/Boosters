package com.willfp.boosters.boosters

import com.willfp.boosters.BoostersPlugin
import com.willfp.boosters.getAmountOfBooster
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.integrations.placeholder.PlaceholderManager
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.savedDisplayName
import com.willfp.libreforge.Holder
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.effects.Effects
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID
import kotlin.math.floor

class Booster(
    private val plugin: BoostersPlugin,
    val config: Config,
) : Holder {
    override val id = config.getString("id")

    val ownedDataKey: PersistentDataKey<Int> = PersistentDataKey(
        plugin.namespacedKeyFactory.create(id),
        PersistentDataKeyType.INT,
        0
    )

    val activeDataKey: PersistentDataKey<String> = PersistentDataKey(
        plugin.namespacedKeyFactory.create("${id}_active"),
        PersistentDataKeyType.STRING,
        ""
    )

    val expiryTimeKey = PersistentDataKey(
        plugin.namespacedKeyFactory.create("${id}_expiry_time"),
        PersistentDataKeyType.DOUBLE,
        0.0
    )

    val active: ActivatedBooster?
        get() {
            val activeKey = Bukkit.getServer().profile.read(activeDataKey)

            if (activeKey.isEmpty()) {
                return null
            }

            val uuid = UUID.fromString(activeKey)

            return ActivatedBooster(this, uuid)
        }

    val secondsLeft: Int
        get() {
            val endTime = Bukkit.getServer().profile.read(expiryTimeKey)
            val currentTime = System.currentTimeMillis()
            return if (endTime < currentTime || active == null) {
                0
            } else {
                ((endTime - currentTime) / 1000).toInt()
            }
        }

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

    val expiryMessages: List<String> = config.getFormattedStrings("messages.expiry")

    val activationCommands: List<String> = config.getFormattedStrings("commands.activation")

    val expiryCommands: List<String> = config.getFormattedStrings("commands.expiry")

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

    override val conditions = Conditions.compile(
        config.getSubsections("conditions"),
        "Booster $id"
    )

    override val effects = Effects.compile(
        config.getSubsections("effects"),
        "Booster $id"
    )

    init {
        Boosters.addNewBooster(this)
        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                plugin,
                "${id}_info"
            ) {
                val active = this.active

                if (active != null) {
                    plugin.langYml.getString("active-placeholder")
                        .replace("%player%", active.player.savedDisplayName)
                        .replace("%booster%", active.booster.name)
                        .formatEco(formatPlaceholders = false)
                } else {
                    plugin.langYml.getString("no-currently-active")
                        .formatEco(formatPlaceholders = false)
                }
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                plugin,
                "${id}_player",
            ) {
                active?.player?.savedDisplayName ?: ""
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                plugin,
                "${id}_active_name",
            ) {
                val active = this.active

                active?.booster?.name ?: ""
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                plugin,
                "${id}_name",
            ) {
                this.name
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                plugin,
                "${id}_seconds_remaining"
            ) {
                secondsLeft.toString()
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                plugin,
                "${id}_time_remaining"
            ) {
                if (secondsLeft <= 0) {
                    return@PlayerlessPlaceholder "00:00:00"
                }

                // if you've seen this code on the internet, no you haven't. shush
                val seconds = secondsLeft % 3600 % 60
                val minutes = floor(secondsLeft % 3600 / 60.0).toInt()
                val hours = floor(secondsLeft / 3600.0).toInt()

                val hh = (if (hours < 10) "0" else "") + hours
                val mm = (if (minutes < 10) "0" else "") + minutes
                val ss = (if (seconds < 10) "0" else "") + seconds

                "${hh}:${mm}:${ss}"
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                plugin,
                "active_list",
            ) {
                var activeList = mutableListOf<String>()

                for(active in Bukkit.getServer().activeBoosters){
                    activeList.add(active.booster.name)
                }

                var outputString = plugin.langYml.getString("no-currently-active-list").formatEco(formatPlaceholders = false)
                if (activeList.size > 0) {
                    outputString = activeList.joinToString(", ")
                }

                outputString
            }
        )
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
