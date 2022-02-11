package com.willfp.boosters.config

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.config.BaseConfig
import com.willfp.eco.core.config.ConfigType

class BoostersYml(plugin: EcoPlugin) : BaseConfig(
    "boosters",
    plugin,
    false,
    ConfigType.YAML
)
