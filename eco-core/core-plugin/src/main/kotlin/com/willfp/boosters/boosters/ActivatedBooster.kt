@file:Suppress("unused")

package com.willfp.boosters.boosters

import com.willfp.eco.core.data.ServerProfile
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.Server
import java.util.*

private val boosters = mutableSetOf<ActivatedBooster>()

fun Server.activateBooster(activatedBooster: ActivatedBooster) {
    val (booster, uuid) = activatedBooster

    ServerProfile.load().write(
        booster.expiryTimeKey,
        (booster.duration.toDouble() * 50) + System.currentTimeMillis()
    )

    ServerProfile.load().write(
        booster.activeDataKey,
        uuid.toString()
    )

    boosters += activatedBooster
}

val Server.activeBoosters: Set<ActivatedBooster>
    get() = boosters.toSet()

fun Server.expireBooster(booster: Booster) {
    boosters.removeIf { it.booster == booster }

    ServerProfile.load().write(
        booster.activeDataKey,
        ""
    )
}

fun Server.scanForBoosters() {
    for (booster in Boosters.values()) {
        val active = booster.active ?: continue
        if (!boosters.contains(active)) {
            boosters += active
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
