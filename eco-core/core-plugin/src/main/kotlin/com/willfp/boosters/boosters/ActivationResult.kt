package com.willfp.boosters.boosters

enum class ActivationResult(val langString: String) {
    ACTIVATED("activated-booster"),
    QUEUED("queued-booster"),
    MERGED("merged-booster"),
    DENIED_CONDITIONS("denied-conditions"),
    INSUFFICIENT_AMOUNT("dont-have")
}

open class BoosterActivationResult(
    val result: ActivationResult,
    val duration: Long
)

object EmptyBoosterActivationResult: BoosterActivationResult(
    ActivationResult.ACTIVATED, 0L
)