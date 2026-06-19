package com.willfp.boosters.boosters

import com.willfp.boosters.expiryWarningIntervals
import com.willfp.boosters.runExpiryWarning

/**
 * Tracks remaining time per active booster and fires expiry warnings as the
 * remaining time crosses each configured threshold.
 *
 * Uses downward-crossing detection rather than a fired-set so that incrementing
 * a booster (which raises the remaining time) re-arms the thresholds and never
 * produces a spurious warning.
 */
object BoosterExpiryWarnings {
    private val lastRemainingTicks = mutableMapOf<Booster, Int>()

    fun tick(booster: Booster) {
        val current = booster.secondsLeft * 20
        val previous = lastRemainingTicks.put(booster, current)

        // previous == null is the first observation: seed state without warning.
        if (!booster.expiryWarningEnabled || previous == null) {
            return
        }

        val crossed = expiryWarningIntervals.any { previous > it && current <= it }
        if (crossed) {
            booster.runExpiryWarning()
        }
    }

    fun remove(booster: Booster) {
        lastRemainingTicks.remove(booster)
    }

    fun clear() {
        lastRemainingTicks.clear()
    }
}