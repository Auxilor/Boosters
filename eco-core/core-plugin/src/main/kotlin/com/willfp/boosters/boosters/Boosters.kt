package com.willfp.boosters.boosters

import com.willfp.boosters.boosters.boosters.Booster15SellMultiplier
import com.willfp.boosters.boosters.boosters.Booster2SellMultiplier
import com.willfp.boosters.boosters.boosters.BoosterSkillXP

object Boosters {
    private val byId = mutableMapOf<String, Booster>()

    val SKILL_XP = BoosterSkillXP()
    val SELL_MULTIPLIER_LOW = Booster15SellMultiplier()
    val SELL_MULTIPLIER_HIGH = Booster2SellMultiplier()

    fun getById(id: String): Booster? {
        return byId[id.lowercase()]
    }

    fun registerNewBooster(booster: Booster) {
        byId[booster.id] = booster
    }

    fun values(): List<Booster> {
        return byId.values.toList()
    }

    fun names(): List<String> {
        return byId.keys.toList()
    }
}