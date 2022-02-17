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
import com.willfp.eco.core.integrations.placeholder.PlaceholderEntry
import com.willfp.eco.core.integrations.placeholder.PlaceholderManager
import com.willfp.eco.util.ListUtils
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.savedDisplayName
import com.willfp.libreforge.LibReforgePlugin
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import java.util.*

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


    override fun handleEnableAdditional() {
        PlaceholderManager.registerPlaceholder(
            PlaceholderEntry(
                this,
                "booster_info",
                {
                    val booster = activeBooster

                    if (booster == null) {
                        return@PlaceholderEntry this.langYml.getString("no-currently-active")
                            .formatEco(formatPlaceholders = false)
                    } else {
                        return@PlaceholderEntry this.langYml.getString("active-placeholder")
                            .replace("%player%", booster.player.savedDisplayName)
                            .replace("%booster%", booster.booster.name)
                            .formatEco(formatPlaceholders = false)
                    }
                },
                false
            )
        )

        PlaceholderManager.registerPlaceholder(
            PlaceholderEntry(
                this,
                "active",
                {
                    activeBooster?.booster?.id ?: ""
                },
                false
            )
        )

        PlaceholderManager.registerPlaceholder(
            PlaceholderEntry(
                this,
                "active_name",
                {
                    activeBooster?.booster?.name ?: ""
                },
                false
            )
        )

        PlaceholderManager.registerPlaceholder(
            PlaceholderEntry(
                this,
                "active_player",
                {
                    activeBooster?.player?.savedDisplayName ?: ""
                },
                false
            )
        )

        this.registerHolderProvider { ListUtils.toSingletonList(activeBooster?.booster) }
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
