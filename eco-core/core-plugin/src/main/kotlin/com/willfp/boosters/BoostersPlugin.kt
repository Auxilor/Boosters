package com.willfp.boosters

import com.willfp.boosters.boosters.ActivatedBooster
import com.willfp.boosters.boosters.Boosters
import com.willfp.boosters.commands.CommandBoosters
import com.willfp.boosters.config.BoostersYml
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.core.data.ServerProfile
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.integrations.placeholder.PlaceholderManager
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import com.willfp.eco.util.ListUtils
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.savedDisplayName
import com.willfp.libreforge.Holder
import com.willfp.libreforge.LibReforgePlugin
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import java.util.UUID
import kotlin.math.floor

class BoostersPlugin : LibReforgePlugin(2036, 14269, "&e") {
    val boostersYml = BoostersYml(this)

    private val boosterKey = PersistentDataKey(
        this.namespacedKeyFactory.create("active_booster"),
        PersistentDataKeyType.STRING,
        ""
    ).server()

    val expiryTimeKey = PersistentDataKey(
        this.namespacedKeyFactory.create("expiry_time"),
        PersistentDataKeyType.DOUBLE,
        0.0
    ).server()

    var activeBooster: ActivatedBooster?
        get() {
            val key = Bukkit.getServer().profile.read(boosterKey)

            return if (key.isEmpty()) {
                null
            } else {
                val booster = key.split("::")
                val id = booster[0]
                val uuid = UUID.fromString(booster[1])
                ActivatedBooster(
                    Boosters.getByID(id) ?: return null,
                    uuid
                )
            }
        }
        set(value) {
            if (value == null) {
                Bukkit.getServer().profile.write(boosterKey, "")
            } else {
                Bukkit.getServer().profile.write(boosterKey, "${value.booster.id}::${value.player.uniqueId}")
            }
        }

    private val secondsLeft: Int
        get() {
            val endTime = ServerProfile.load().read(expiryTimeKey)
            val currentTime = System.currentTimeMillis()
            return if (endTime < currentTime || activeBooster == null) {
                0
            } else {
                ((endTime - currentTime) / 1000).toInt()
            }
        }

    override fun handleEnableAdditional() {
        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                this,
                "booster_info"
            ) {
                val booster = activeBooster

                if (booster == null) {
                    return@PlayerlessPlaceholder this.langYml.getString("no-currently-active")
                        .formatEco(formatPlaceholders = false)
                } else {
                    return@PlayerlessPlaceholder this.langYml.getString("active-placeholder")
                        .replace("%player%", booster.player.savedDisplayName)
                        .replace("%booster%", booster.booster.name)
                        .formatEco(formatPlaceholders = false)
                }
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                this,
                "active",
            ) {
                activeBooster?.booster?.id ?: ""
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                this,
                "active_name"
            ) {
                activeBooster?.booster?.name ?: ""
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                this,
                "active_player",
            ) {
                activeBooster?.player?.savedDisplayName ?: ""
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                this,
                "seconds_remaining"
            ) {
                secondsLeft.toString()
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                this,
                "time_remaining"
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

        this.registerHolderProvider {
            @Suppress("USELESS_CAST", "UNCHECKED_CAST") // No idea why it's not happy.
            ListUtils.toSingletonList(activeBooster?.booster as? Holder) as Iterable<Holder>
        }
    }

    override fun handleReloadAdditional() {
        this.scheduler.runTimer(1, 1) {
            val booster = activeBooster ?: return@runTimer
            val endTime = ServerProfile.load().read(expiryTimeKey)
            if (endTime <= System.currentTimeMillis()) {
                for (expiryMessage in booster.booster.expiryMessages) {
                    Bukkit.broadcastMessage(expiryMessage)
                }
                activeBooster = null
            }
        }
    }

    override fun loadListeners(): List<Listener> {
        return listOf(

        )
    }

    override fun loadPluginCommands(): List<PluginCommand> {
        return listOf(
            CommandBoosters(this)
        )
    }

    override fun getMinimumEcoVersion(): String {
        return "6.24.0"
    }

    init {
        instance = this
    }

    companion object {
        lateinit var instance: BoostersPlugin
    }
}
