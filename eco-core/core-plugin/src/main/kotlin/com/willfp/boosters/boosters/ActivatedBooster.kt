@file:Suppress("unused")

package com.willfp.boosters.boosters

import com.willfp.eco.core.data.ServerProfile
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.Server
import java.util.*

private val boosters = mutableSetOf<ActivatedBooster>()

fun Server.addActiveBooster(activatedBooster: ActivatedBooster) {
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

data class ActivatedBooster(
    val booster: Booster,
    private val uuid: UUID
) {
    val player: OfflinePlayer
        get() = Bukkit.getOfflinePlayer(uuid)

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
