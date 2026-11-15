package com.willfp.boosters.libreforge

import com.willfp.boosters.boosters.Boosters
import wiki.ecohub.config.ArgType
import wiki.ecohub.config.Config
import wiki.ecohub.config.arguments
import wiki.ecohub.core.registry.NoCompileData
import wiki.ecohub.rules.condition.Condition
import wiki.ecohub.rules.holder.Dispatcher
import wiki.ecohub.rules.holder.ProvidedHolder

object ConditionIsBoosterActive : Condition<NoCompileData>("is_booster_active") {
    override val description = "Passes when the specified booster is currently active."

    override val categories = setOf("world")

    override val arguments = arguments {
        require(
            "booster",
            "You must specify the booster!",
            description = "The ID of the booster to check.",
            type = ArgType.STRING,
            example = "2sell_multiplier"
        )
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
