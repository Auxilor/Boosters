package com.willfp.boosters.boosters

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.ImmutableList
import com.willfp.boosters.BoostersPlugin
import com.willfp.eco.core.config.ConfigType
import com.willfp.eco.core.config.readConfig
import com.willfp.eco.core.config.updating.ConfigUpdater
import com.willfp.libreforge.chains.EffectChains
import java.io.File

object Boosters {
    /** Registered boosters. */
    private val BY_ID: BiMap<String, Booster> = HashBiMap.create()

    /**
     * Get all registered [Booster]s.
     *
     * @return A list of all [Booster]s.
     */
    @JvmStatic
    fun values(): List<Booster> {
        return ImmutableList.copyOf(BY_ID.values)
    }

    /**
     * Get [Booster] matching ID.
     *
     * @param name The name to search for.
     * @return The matching [Booster], or null if not found.
     */
    @JvmStatic
    fun getByID(name: String): Booster? {
        return BY_ID[name]
    }

    /**
     * Update all [Booster]s.
     *
     * @param plugin Instance of Booster.
     */
    @ConfigUpdater
    @JvmStatic
    fun update(plugin: BoostersPlugin) {
        for (booster in values()) {
            removeBooster(booster)
        }

        for ((id, config) in plugin.fetchConfigs("boosters")) {
            Booster(plugin, id, config)
        }

        // Legacy
        val boostersYml = File(plugin.dataFolder, "boosters.yml").readConfig(ConfigType.YAML)

        for (config in boostersYml.getSubsections("boosters")) {
            Booster(plugin, config.getString("id"), config)
        }
        boostersYml.getSubsections("chains").mapNotNull {
            EffectChains.compile(it, "Effect Chains")
        }
    }

    /**
     * Add new [Booster] to Booster.
     *
     * @param booster The [Booster] to add.
     */
    @JvmStatic
    fun addNewBooster(booster: Booster) {
        BY_ID.remove(booster.id)
        BY_ID[booster.id] = booster
    }

    /**
     * Remove [Booster] from Booster.
     *
     * @param booster The [Booster] to remove.
     */
    @JvmStatic
    fun removeBooster(booster: Booster) {
        BY_ID.remove(booster.id)
    }
}
