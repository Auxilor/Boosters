package com.willfp.boosters.libreforge

import com.willfp.boosters.boosters.Boosters
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.Dispatcher
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.ProvidedHolder
import com.willfp.libreforge.arguments
import com.willfp.libreforge.conditions.Condition
import org.bukkit.entity.Player

object ConditionIsBoosterActive : Condition<NoCompileData>("is_booster_active") {
    override val arguments = arguments {
        require("booster", "You must specify the booster!")
    }

    override fun isMet(
        dispatcher: Dispatcher<*>,
        config: Config,
        holder: ProvidedHolder,
        compileData: NoCompileData
    ): Boolean {
        return Boosters.getByID(config.getString("booster"))?.active != null
    }
}
