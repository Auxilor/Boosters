package com.willfp.boosters.data

import com.willfp.boosters.BoostersPlugin
import org.bukkit.Bukkit

class SaveHandler {
    companion object {
        fun save(plugin: BoostersPlugin) {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                return
            }
            if (plugin.configYml.getBool("log-autosaves")) {
                plugin.logger.info("Auto-Saving player data!")
            }
            plugin.dataYml.save()
            if (plugin.configYml.getBool("log-autosaves")) {
                plugin.logger.info("Saved data!")
            }
        }
    }

    class Runnable(
        private val plugin: BoostersPlugin
    ) : java.lang.Runnable {
        override fun run() {
            save(plugin)
        }
    }
}