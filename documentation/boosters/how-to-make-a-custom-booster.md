---
title: How to make a Booster
sidebar_position: 1
---
Boosters are a great way to give players a temporary boost in the game, such as a sell multiplier or a resource boost. They can be activated by players to enhance their gameplay experience.

## Creating a Booster

Each booster has its own config file, placed in the `/boosters/` folder, and you can add or remove them as you please. There's an example config called `_example.yml` to help you out!

The ID of the booster is the file name. This is what you use in commands and in the [Item Lookup System](https://plugins.auxilor.io/the-item-lookup-system).
ID's must be lowercase letters, numbers, and underscores only.

## Example Booster Config

```yaml
name: "1.5x Sell Multiplier" 
duration: 72000 
category: "sell_multipliers"
merge-tag: "sell_multiplier_1.5x"

bossbar:
  enabled: true
  name: "&a1.5x Sell Multiplier &7(%time_remaining%)"
  color: GREEN
  style: SOLID

activation-effects:
  - id: send_message
    args:
      action_bar: false
      messages:
        - ""
        - " %activator%&f has activated a &a1.5x Sell Multiplier Booster&f!"
        - " &fThis booster will last an hour, be sure to thank them!"
        - ""

increment-effects:
  - id: send_message
    args:
      action_bar: false
      messages:
        - ""
        - " %activator%&f has increased the &a1.5x Sell Multiplier Booster's duration&f!"
        - " &fThis booster will last another hour, be sure to thank them!"
        - ""

queue-effects:
  - id: send_message
    args:
      action_bar: false
      messages:
        - ""
        - " %activator%&f has queued a &a1.5x Sell Multiplier Booster&f!"
        - " &fThis booster will last %time%, when its time comes!"
        - ""

queue-increment-effects:
  - id: send_message
    args:
      action_bar: false
      messages:
        - ""
        - " %activator%&f has increased a &a1.5x Sell Multiplier Booster&f in the queue!"
        - " &fThis booster will now last %time%, when its time comes!"
        - ""

expiry-effects:
  - id: send_message
    args:
      action_bar: false
      messages:
        - ""
        - " &fThe &a1.5x Sell Multiplier Booster&f has ended"
        - " &fGet another one here: &ahttps://store.ecomc.net/package/756887"
        - ""


effects:
  - id: sell_multiplier
    args:
      multiplier: 1.5

conditions: []

gui:
  item: player_head texture:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTM0YjI3YmZjYzhmOWI5NjQ1OTRiNjE4YjExNDZhZjY5ZGUyNzhjZTVlMmUzMDEyY2I0NzFhOWEzY2YzODcxIn19fQ==
  name: "&d1.5x Sell Multiplier"
  lore:
    - ""
    - "&fGives everyone online a"
    - "&a1.5x Sell Multiplier"
    - "&fto make money faster!"
    - ""
    - "&fDuration: &a1 Hour"
    - ""
    - "&fYou have: &a%amount%"
    - "&fGet more at &astore.ecomc.net"
    - ""
    - "&e&oClick to activate!"
    - ""
  position:
    row: 2
    column: 2
```

## Understanding all the sections

### The Booster Info Section
```yaml
name: "2x Sell Multiplier" # The display name of the Booster.
duration: 72000 # The duration (in ticks) of the Booster. (e.g. 6000 = 5 minutes)
category: "sell_multipliers" # (Optional) The category of the booster, used for queueing and preventing boosters of the same category from being active at the same time.
merge-tag: "sell_multiplier_1.5x" # (Optional) The tag used to identify boosters that can be merged together to increase duration instead of activating a new booster.
```

### The Bossbar Section
```yaml
# Optional per-booster bossbar settings.
# The bossbar progress tracks remaining duration and updates if the booster is incremented.
bossbar:
  enabled: true
  name: "&a1.5x Sell Multiplier" # Defaults to booster "name" if omitted. You can use %time_remaining% here to show the remaining time on the bossbar.
  color: GREEN # Valid colors: PINK, BLUE, RED, GREEN, YELLOW, PURPLE, WHITE
  style: SOLID # Valid styles: SOLID, SEGMENTED_6, SEGMENTED_10, SEGMENTED_12, SEGMENTED_20
```

### The Activation, Increment and Expiry Section
:::danger Effects Section

The effects section is the core functionality of the booster. You can configure effects, conditions, filters, mutators and triggers in this section to run when the booster is activated, incremented, queued, or expires.

Check out [Configuring an Effect](https://plugins.auxilor.io/effects/configuring-an-effect) to understand how to configure this section correctly.

For more advanced users or setups, you can configure chains in this section to string together different effects under one trigger. Check out [Configuring an Effect Chain](https://plugins.auxilor.io/effects/configuring-a-chain) for more info.

:::
```yaml
# Effects to be run when the Booster is activated (applies to all players)
# %activator% - The player who activated the booster
# %player% - The player receiving the message/effect
activation-effects:
  - id: send_message
    args:
      action_bar: false
      messages:
        - ""
        - " %activator%&f has activated a &a1.5x Sell Multiplier Booster&f!"
        - " &fThis booster will last an hour, be sure to thank them!"
        - ""

# Effects to be run when the Booster is incremented (applies to all players)
increment-effects:
  - id: send_message
    args:
      action_bar: false
      messages:
        - ""
        - " %activator%&f has increased the &a1.5x Sell Multiplier Booster's duration&f!"
        - " &fThis booster will last another hour, be sure to thank them!"
        - ""

# Effects to be run when the Booster is queued (applies to all players)
queue-effects:
  - id: send_message
    args:
      action_bar: false
      messages:
        - ""
        - " %activator%&f has queued a &a1.5x Sell Multiplier Booster&f!"
        - " &fThis booster will last %time%, when its time comes!"
        - ""


# Effects to be run when the Booster is incremented in the queue (applies to all players)
queue-increment-effects:
  - id: send_message
    args:
      action_bar: false
      messages:
        - ""
        - " %activator%&f has increased a &a1.5x Sell Multiplier Booster&f in the queue!"
        - " &fThis booster will now last %time%, when its time comes!"
        - ""

# Effects to be run when the Booster expires (applies to all players)
expiry-effects:
  - id: send_message
    args:
      action_bar: false
      messages:
        - ""
        - " &fThe &a1.5x Sell Multiplier Booster&f has ended"
        - " &fGet another one here: &ahttps://store.ecomc.net/package/756887"
        - ""
```

### The Effects Section
:::danger Effects Section

The effects section is the core functionality of the booster. You can configure effects, conditions, filters, mutators and triggers in this section to run whilst the booster is active.

Check out [Configuring an Effect](https://plugins.auxilor.io/effects/configuring-an-effect) to understand how to configure this section correctly.

For more advanced users or setups, you can configure chains in this section to string together different effects under one trigger. Check out [Configuring an Effect Chain](https://plugins.auxilor.io/effects/configuring-a-chain) for more info.

:::
```yaml
# The effects whilst the Booster is active (i.e. the functionality)
effects:
  - id: sell_multiplier
    args:
      multiplier: 2

# The conditions required for the effects to activate
conditions: [ ]
```

### The Booster GUI Section
```yaml
gui:
  item: player_head texture:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjBhN2I5NGM0ZTU4MWI2OTkxNTlkNDg4NDZlYzA5MTM5MjUwNjIzN2M4OWE5N2M5MzI0OGEwZDhhYmM5MTZkNSJ9fX0= # The GUI item: https://plugins.auxilor.io/the-item-lookup-system
  name: "&d2x Sell Multiplier" # The name of the Booster in the GUI.
  lore: # The lore of the Booster in the GUI.
    - ""
    - "&fGives everyone online a"
    - "&a2x Sell Multiplier"
    - "&fto make money faster!"
    - ""
    - "&fDuration: &a1 Hour"
    - ""
    - "&fYou have: &a%amount%"
    - "&fGet more at &astore.ecomc.net"
    - ""
    - "&e&oClick to activate!"
    - ""
  position:
    row: 2 # 1-6
    column: 5 # 1-9
```

## Internal Placeholders

| Placeholder        | Value                                                      |
|--------------------|------------------------------------------------------------|
| `%amount%`         | The amount of the booster the player has (For use in GUI)  |
| `%activator%`      | The player who activated the booster (For use in messages) |
| `%time%`           | The time the booster is activated/incremented for          |
| `%time_remaining%` | The time remaining on the booster (for use in Bossbar)     |


<hr/>

## Default Configs

The default configs can be found [here](https://github.com/Auxilor/Boosters/tree/master/eco-core/core-plugin/src/main/resources/boosters). <br/>
You can find additional user-created configs on [lrcdb](https://lrcdb.auxilor.io/).