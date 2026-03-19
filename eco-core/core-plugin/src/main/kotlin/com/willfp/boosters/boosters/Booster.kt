package com.willfp.boosters.boosters

import com.willfp.boosters.getAmountOfBooster
import com.willfp.boosters.plugin
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.integrations.placeholder.PlaceholderManager
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import com.willfp.eco.core.registry.Registrable
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.savedDisplayName
import com.willfp.libreforge.Holder
import com.willfp.libreforge.ViolationContext
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.effects.Effects
import com.willfp.libreforge.effects.executors.impl.NormalExecutorFactory
import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.Locale
import java.util.UUID
import kotlin.math.floor

class Booster(
    id: String,
    val config: Config
) : Holder, Registrable {
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

    val totalDurationKey = PersistentDataKey(
        plugin.namespacedKeyFactory.create("${id}_total_duration"),
        PersistentDataKeyType.DOUBLE,
        0.0
    )

    val category: String? = config.getStringOrNull("category")

    val mergeTag: String? = config.getStringOrNull("merge-tag")

    val active: ActivatedBooster?
        get() {
            val activeKey = Bukkit.getServer().profile.read(activeDataKey)

            if (activeKey.isEmpty()) {
                return null
            }

            val uuid = try {
                UUID.fromString(activeKey)
            } catch (_: IllegalArgumentException) {
                null
            }

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

    val canBeActivated: Boolean
        get() {
            return active == null && Bukkit.getServer().activeBoosters.none { it.booster.category == this.category }
        }

    fun canBeMerged(booster: Booster): Boolean {
        if (booster.id == this.id) {
            return true
        }

        if (this.mergeTag == null || booster.mergeTag == null) {
            return false
        }

        return this.mergeTag == booster.mergeTag
    }

    fun isCategorizedWith(booster: Booster): Boolean {
        if (this.category == null || booster.category == null) {
            return false
        }
        return this.category == booster.category
    }

    val name = config.getFormattedString("name")

    val bossBarEnabled = config.getBool("bossbar.enabled")

    private val bossBarNameTemplate = if (config.has("bossbar.name")) {
        config.getFormattedString("bossbar.name")
    } else {
        null
    }

    val bossBarName: String
        get() = getCurrentBossBarName()

    fun getCurrentBossBarName(): String {
        return if (bossBarNameTemplate != null) {
            bossBarNameTemplate
                .replace("%time_remaining%", getFormattedTimeLeft())
                .formatEco(formatPlaceholders = true)
        } else {
            name
        }
    }

    val bossBarColor = parseBarColor(config.getString("bossbar.color"))

    val bossBarStyle = parseBarStyle(config.getString("bossbar.style"))

    val duration = config.getInt("duration")

    val bossBarProgress: Double
        get() {
            if (active == null) {
                return 0.0
            }

            val endTime = Bukkit.getServer().profile.read(expiryTimeKey)
            val remainingMillis = (endTime - System.currentTimeMillis()).coerceAtLeast(0.0)
            val totalMillis = Bukkit.getServer().profile.read(totalDurationKey)
                .coerceAtLeast(1.0)

            return (remainingMillis / totalMillis).coerceIn(0.0, 1.0)
        }

    val activationEffects = Effects.compileChain(
        config.getSubsections("activation-effects"),
        NormalExecutorFactory.create(),
        ViolationContext(plugin, "Booster $id Activation Effects")
    )

    val queueEffects = Effects.compileChain(
        config.getSubsections("queue-effects"),
        NormalExecutorFactory.create(),
        ViolationContext(plugin, "Booster $id Queue Effects")
    )

    val expiryEffects = Effects.compileChain(
        config.getSubsections("expiry-effects"),
        NormalExecutorFactory.create(),
        ViolationContext(plugin, "Booster $id Expiry Effects")
    )

    val incrementEffects = Effects.compileChain(
        config.getSubsections("increment-effects"),
        NormalExecutorFactory.create(),
        ViolationContext(plugin, "Booster $id Increment Effects")
    )

    val queueIncrementEffects = Effects.compileChain(
        config.getSubsections("queue-increment-effects"),
        NormalExecutorFactory.create(),
        ViolationContext(plugin, "Booster $id Increment Effects")
    )


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

    fun getFormattedTimeLeft(overrideTime: Int? = null): String {
        val secLeft = overrideTime ?: secondsLeft

        if (secLeft <= 0) {
            return "00:00:00"
        }

        // if you've seen this code on the internet, no you haven't. shush
        val seconds = secLeft % 3600 % 60
        val minutes = floor(secLeft % 3600 / 60.0).toInt()
        val hours = floor(secLeft / 3600.0).toInt()

        val hh = (if (hours < 10) "0" else "") + hours
        val mm = (if (minutes < 10) "0" else "") + minutes
        val ss = (if (seconds < 10) "0" else "") + seconds

        return "${hh}:${mm}:${ss}"
    }

    val guiRow = config.getInt("gui.position.row")

    val guiColumn = config.getInt("gui.position.column")

    override val conditions = Conditions.compile(
        config.getSubsections("conditions"),
        ViolationContext(plugin, "Booster $id conditions")
    )

    val activationConditions = Conditions.compile(
        config.getSubsections("activation-conditions"),
        ViolationContext(plugin, "Booster $id activation conditions")
    )

    override val effects = Effects.compile(
        config.getSubsections("effects"),
        ViolationContext(plugin, "Booster $id effects")
    )

    override val id = plugin.createNamespacedKey(id)

    init {
        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                plugin,
                "${id}_info"
            ) {
                val active = this.active

                if (active != null) {
                    plugin.langYml.getString("active-placeholder")
                        .replace("%player%", active.player?.savedDisplayName ?: plugin.langYml.getString("console-displayname"))
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
                val active = this.active

                if (active == null) {
                    ""
                } else {
                    active.player?.savedDisplayName ?: plugin.langYml.getString("console-displayname").formatEco(formatPlaceholders = false)
                }
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                plugin,
                "${id}_active_name",
            ) {
                val active = this.active

                if (active == null) {
                    ""
                } else {
                    active.player?.name ?: plugin.langYml.getString("console-displayname").formatEco(formatPlaceholders = false)
                }
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

                for (active in Bukkit.getServer().activeBoosters) {
                    activeList.add(active.booster.name)
                }

                var outputString =
                    plugin.langYml.getString("no-currently-active-list").formatEco(formatPlaceholders = false)
                if (activeList.size > 0) {
                    outputString = activeList.joinToString(", ")
                }

                outputString
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                plugin,
                "active_ids_list",
            ) {
                var activeList = mutableListOf<String>()

                for (active in Bukkit.getServer().activeBoosters) {
                    activeList.add(active.booster.getID())
                }

                var outputString =
                    plugin.langYml.getString("no-currently-active-ids-list").formatEco(formatPlaceholders = false)
                if (activeList.size > 0) {
                    outputString = activeList.joinToString(",")
                }

                outputString
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                plugin,
                "time_remaining",
            ) {
                val currentActive = Bukkit.getServer().activeBoosters.firstOrNull()

                if (currentActive == null) {
                    "00:00:00"
                } else {
                    currentActive.booster.getFormattedTimeLeft()
                }
            }
        )
    }

    override fun getID(): String {
        return this.id.key
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

    private fun parseBarColor(raw: String): BarColor {
        if (raw.isBlank()) {
            return BarColor.WHITE
        }

        val normalized = raw.trim()
            .replace("-", "_")
            .replace(" ", "_")
            .uppercase(Locale.ROOT)

        return try {
            BarColor.valueOf(normalized)
        } catch (_: IllegalArgumentException) {
            plugin.logger.warning("Invalid bossbar color '$raw' for booster '${id.key}', defaulting to WHITE.")
            BarColor.WHITE
        }
    }

    private fun parseBarStyle(raw: String): BarStyle {
        if (raw.isBlank()) {
            return BarStyle.SOLID
        }

        val normalized = raw.trim()
            .replace("-", "_")
            .replace(" ", "_")
            .uppercase(Locale.ROOT)

        return try {
            BarStyle.valueOf(normalized)
        } catch (_: IllegalArgumentException) {
            plugin.logger.warning("Invalid bossbar style '$raw' for booster '${id.key}', defaulting to SOLID.")
            BarStyle.SOLID
        }
    }
}
