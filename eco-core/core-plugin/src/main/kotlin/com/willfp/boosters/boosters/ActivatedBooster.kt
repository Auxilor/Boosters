@file:Suppress("unused")

package com.willfp.boosters.boosters

import com.willfp.eco.core.data.ServerProfile
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.Server
import java.util.Objects
import java.util.UUID

private val boosters = mutableSetOf<ActivatedBooster>()

fun Server.increaseBooster(booster: Booster) {
    val profile = ServerProfile.load()
    val extraTime = booster.duration.toDouble() * 50

    val currentExpiry = profile.read(booster.expiryTimeKey)
    profile.write(booster.expiryTimeKey, currentExpiry + extraTime)

    val currentTotalDuration = profile.read(booster.totalDurationKey)
    val fallbackTotalDuration = (currentExpiry - System.currentTimeMillis()).coerceAtLeast(0.0)
    val baseDuration = if (currentTotalDuration > 0) currentTotalDuration else fallbackTotalDuration

    profile.write(booster.totalDurationKey, baseDuration + extraTime)
    val newExpiry = currentExpiry + extraTime
    profile.write(booster.expiryTimeKey, newExpiry)
}


fun Server.activateBooster(activatedBooster: ActivatedBooster) {
    val (booster, uuid) = activatedBooster
    val profile = ServerProfile.load()

    val durationMillis = booster.duration.toDouble() * 50

    profile.write(
        booster.expiryTimeKey,
        durationMillis + System.currentTimeMillis()
    )

    profile.write(
        booster.totalDurationKey,
        durationMillis
    )

    profile.write(
        booster.activeDataKey,
        uuid.toString()
    )

    boosters += activatedBooster
}

val Server.activeBoosters: Set<ActivatedBooster>
    get() = boosters.toSet()

fun Server.expireBooster(booster: Booster) {
    boosters.removeIf { it.booster == booster }

    val profile = ServerProfile.load()

    profile.write(
        booster.activeDataKey,
        ""
    )

    profile.write(
        booster.expiryTimeKey,
        0.0
    )

    profile.write(
        booster.totalDurationKey,
        0.0
    )
}

fun Server.scanForBoosters() {
    val profile = ServerProfile.load()

    for (booster in Boosters.values()) {
        val active = booster.active ?: continue
        if (!boosters.contains(active)) {
            boosters += active
        }

        if (profile.read(booster.totalDurationKey) <= 0.0) {
            val remaining = (profile.read(booster.expiryTimeKey) - System.currentTimeMillis()).coerceAtLeast(0.0)
            profile.write(booster.totalDurationKey, remaining)
        }
    }
}

data class ActivatedBooster(
    val booster: Booster,
    val uuid: UUID?
) {
    val player: OfflinePlayer?
        get() = uuid?.let { Bukkit.getOfflinePlayer(it) }

    override fun equals(other: Any?): Boolean {
        if (other !is ActivatedBooster) {
            return false
        }

        return other.booster == this.booster
    }

    override fun hashCode(): Int {
        return Objects.hash(this.booster)
    }
}
