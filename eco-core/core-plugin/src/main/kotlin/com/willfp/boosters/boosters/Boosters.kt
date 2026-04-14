package com.willfp.boosters.boosters

import com.google.common.collect.ImmutableList
import com.willfp.boosters.gui.BoosterGUI
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.registry.Registry
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import com.willfp.libreforge.loader.configs.LegacyLocation
import com.willfp.boosters.boosters.activeBoosters
import com.willfp.boosters.plugin
import com.willfp.eco.core.integrations.placeholder.PlaceholderManager
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import com.willfp.eco.util.formatEco
import org.bukkit.Bukkit

object Boosters : ConfigCategory("booster", "boosters") {
    /** Registered boosters. */
    private val registry = Registry<Booster>()
    private var cachedValues: List<Booster> = emptyList()

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
        return cachedValues
    }

    /**
     * Get all unique categories of boosters.
     *
     * @return A set of all booster categories.
     */
    @JvmStatic
    fun getCategories(): Set<String> {
        return registry.values().mapNotNull { it.category }.toSet()
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
        cachedValues = emptyList()
    }

    override fun acceptConfig(plugin: LibreforgePlugin, id: String, config: Config) {
        registry.register(Booster(id, config))
        cachedValues = ImmutableList.copyOf(registry.values())
    }

    override fun afterReload(plugin: LibreforgePlugin) {
        registerGlobalPlaceholders()
        BoosterGUI.update()
    }

    fun registerGlobalPlaceholders() {
        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                plugin,
                "active_list",
            ) {
                val activeList = Bukkit.getServer().activeBoosters.map { it.booster.name }

                if (activeList.isEmpty()) {
                    plugin.langYml.getString("no-currently-active-list").formatEco(formatPlaceholders = false)
                } else {
                    activeList.joinToString(", ")
                }
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                plugin,
                "active_ids_list",
            ) {
                val activeList = Bukkit.getServer().activeBoosters.map { it.booster.getID() }

                if (activeList.isEmpty()) {
                    plugin.langYml.getString("no-currently-active-ids-list").formatEco(formatPlaceholders = false)
                } else {
                    activeList.joinToString(",")
                }
            }
        )

        PlaceholderManager.registerPlaceholder(
            PlayerlessPlaceholder(
                plugin,
                "time_remaining",
            ) {
                val currentActive = Bukkit.getServer().activeBoosters.firstOrNull()

                currentActive?.booster?.getFormattedTimeLeft() ?: "00:00:00"
            }
        )
    }
}
