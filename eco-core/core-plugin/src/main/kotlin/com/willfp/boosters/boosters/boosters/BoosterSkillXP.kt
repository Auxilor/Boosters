package com.willfp.boosters.boosters.boosters

import com.willfp.boosters.BoostersPlugin
import com.willfp.boosters.activeBooster
import com.willfp.boosters.boosters.Booster
import com.willfp.ecoskills.api.PlayerSkillExpGainEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority

class BoosterSkillXP: Booster(
    BoostersPlugin.instance,
    "skill_xp"
) {
    override val duration = 72000

    @EventHandler(priority = EventPriority.HIGH)
    fun onGainSkillXP(event: PlayerSkillExpGainEvent) {
        if (Bukkit.getServer().activeBooster?.booster != this) {
            return
        }

        if (event.isCancelled) {
            return
        }

        event.amount *= 2
    }
}