package com.willfp.boosters.boosters

import com.google.common.collect.ImmutableList
import com.willfp.boosters.BoostersPlugin
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.registry.Registry
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import com.willfp.libreforge.loader.configs.LegacyLocation

object Boosters : ConfigCategory("booster", "boosters") {
    /** Registered boosters. */
    private val registry = Registry<Booster>()

    override val legacyLocation = LegacyLocation(
        "boosters.yml",
        "boosters"
    )

    /**
     * Get all registered [Booster]s.
     *
     * @return A list of all [Booster]s.
     */
    @JvmStatic
    fun values(): List<Booster> {
        return ImmutableList.copyOf(registry.values())
    }

    /**
     * Get [Booster] matching ID.
     *
     * @param name The name to search for.
     * @return The matching [Booster], or null if not found.
     */
    @JvmStatic
    fun getByID(name: String): Booster? {
        return registry[name]
    }

    override fun clear(plugin: LibreforgePlugin) {
        registry.clear()
    }

    override fun acceptConfig(plugin: LibreforgePlugin, id: String, config: Config) {
        registry.register(Booster(plugin as BoostersPlugin, id, config))
    }
}
