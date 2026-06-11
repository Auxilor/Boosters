---
title: "How to Make a Booster"
sidebar_position: 1
---

A **booster** is a buyable reward that, once activated, applies an effect to every online player for a set **duration**, such as a sell multiplier or a resource boost. Each booster is one config file, and the file name is its **ID**. This page walks you through creating one and explains every part of the config you can set.

## Quick start

1. Open the `/boosters/` folder in the plugin's data directory.
2. Copy the bundled `_example.yml` to a new file, e.g. `sell_multiplier_1.5x.yml`. **The file name becomes the booster's ID.**
3. Edit the `name`, `duration`, and `effects` to taste (see the sections below).
4. Run `/boosters reload` in-game to load the change.
5. Give it to yourself with `/boosters give <you> sell_multiplier_1.5x`, then open `/boosters`; the booster should appear in the menu, ready to activate.

:::tip
`_example.yml` is included as a reference and is **never loaded**, so copy or rename it to make a real booster. You can also organise boosters into subfolders inside `boosters/`, and they'll still load.
:::

## Naming and IDs

The **file name (without `.yml`) is the booster's ID**. So `sell_multiplier_1.5x.yml` has the ID `sell_multiplier_1.5x`.

That ID is what you use in commands (`/boosters give <player> sell_multiplier_1.5x`) and in the [Item Lookup System](https://plugins.auxilor.io/the-item-lookup-system).

:::warning ID rules
IDs may only contain **lowercase letters, numbers, and underscores** (`a-z`, `0-9`, `_`). No spaces, capitals, or hyphens, or the booster will not load.
:::

## The structure of a booster

A config has four logical parts, top to bottom:

| Part | What it controls |
| --- | --- |
| **Info** | The name, duration, category, and merge behaviour |
| **Bossbar** | The optional on-screen timer |
| **Effects** | What runs on activation, increment, queue, and expiry, and while active |
| **GUI** | How the booster looks and where it sits in the `/boosters` menu |

The rest of this page covers each part in detail. Here's a complete example with everything in place:

```yaml
# === Info: identity and duration ===
name: "1.5x Sell Multiplier" # Display name of the booster
duration: 72000 # Duration in ticks; 20 ticks = 1 second, so 72000 = 1 hour
category: "sell_multipliers" # Optional; boosters in the same category queue instead of running together
merge-tag: "sell_multiplier_1.5x" # Optional; matching tags merge to extend duration instead of stacking

# === Bossbar: optional on-screen timer ===
bossbar:
  enabled: true # Set false to hide the bossbar for this booster
  name: "&a1.5x Sell Multiplier &7(%time_remaining%)" # Defaults to the booster name if omitted
  color: GREEN # PINK, BLUE, RED, GREEN, YELLOW, PURPLE, or WHITE
  style: SOLID # SOLID, SEGMENTED_6, SEGMENTED_10, SEGMENTED_12, or SEGMENTED_20

# === Effects: lifecycle messages and the active functionality ===
activation-effects: # Run when the booster is activated (applies to every online player)
  - id: send_message
    args:
      messages:
        - " %activator%&f has activated a &a1.5x Sell Multiplier Booster&f!"
increment-effects: [] # Run when an active booster's duration is extended
queue-effects: [] # Run when the booster is queued behind an active one in its category
queue-increment-effects: [] # Run when a queued booster's duration is extended
expiry-effects: [] # Run when the booster ends
effects: # Run for the whole duration; this is the booster's functionality
  - id: sell_multiplier
    args:
      multiplier: 1.5
conditions: [] # Conditions required for the effects to apply ([] = always)

# === GUI: how it appears in /boosters ===
gui:
  item: player_head texture:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTM0YjI3YmZjYzhmOWI5NjQ1OTRiNjE4YjExNDZhZjY5ZGUyNzhjZTVlMmUzMDEyY2I0NzFhOWEzY2YzODcxIn19fQ== # Item Lookup System
  name: "&d1.5x Sell Multiplier" # Name shown in the GUI
  lore:
    - "&fGives everyone online a 1.5x Sell Multiplier"
    - "&fYou have: &a%amount%"
  position:
    page: 1 # Which menu page to show on
    row: 2 # 1 to 6
    column: 2 # 1 to 9
```

### Info

The top-level fields that identify the booster and control how long it lasts.

```yaml
name: "1.5x Sell Multiplier" # Display name of the booster
duration: 72000 # Duration in ticks; 20 ticks = 1 second, so 72000 = 1 hour
category: "sell_multipliers" # Optional; boosters in the same category queue instead of running together
merge-tag: "sell_multiplier_1.5x" # Optional; matching tags merge to extend duration instead of stacking
```

### Bossbar

An optional on-screen timer whose progress tracks the remaining duration and updates when the booster is incremented.

```yaml
bossbar:
  enabled: true # Set false to hide the bossbar for this booster
  name: "&a1.5x Sell Multiplier &7(%time_remaining%)" # Defaults to the booster name if omitted
  color: GREEN # PINK, BLUE, RED, GREEN, YELLOW, PURPLE, or WHITE
  style: SOLID # SOLID, SEGMENTED_6, SEGMENTED_10, SEGMENTED_12, or SEGMENTED_20
```

### Effects

The lifecycle blocks run effects at each point in the booster's life; the `effects` block is what the booster does while it is active, gated by `conditions`.

```yaml
# %activator% is the player who activated the booster; %player% is each player receiving the effect
activation-effects: # Run when the booster is activated
  - id: send_message
    args:
      messages:
        - " %activator%&f has activated a &a1.5x Sell Multiplier Booster&f!"
increment-effects: [] # Run when an active booster's duration is extended
queue-effects: [] # Run when the booster is queued behind an active one in its category
queue-increment-effects: [] # Run when a queued booster's duration is extended
expiry-effects: [] # Run when the booster ends

effects: # Run for the whole duration; this is the booster's functionality
  - id: sell_multiplier
    args:
      multiplier: 1.5
conditions: [] # Conditions required for the effects to apply ([] = always)
```

:::danger Effects are their own system
Effects, conditions, filters, mutators, triggers, and chains are a shared eco system, not specific to Boosters, with hundreds of options. They are **not** documented here, so see the dedicated guides:

- [Configuring an Effect](https://plugins.auxilor.io/effects/configuring-an-effect) is the full effect, trigger, and condition reference.
- [Configuring an Effect Chain](https://plugins.auxilor.io/effects/configuring-a-chain) strings multiple effects under one trigger for advanced boosters.
:::

### GUI

How the booster is rendered in the `/boosters` menu and where it sits.

```yaml
gui:
  item: player_head texture:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTM0YjI3YmZjYzhmOWI5NjQ1OTRiNjE4YjExNDZhZjY5ZGUyNzhjZTVlMmUzMDEyY2I0NzFhOWEzY2YzODcxIn19fQ== # GUI item, from the Item Lookup System
  name: "&d1.5x Sell Multiplier" # Name shown in the GUI
  lore: # Lore lines shown in the GUI; %amount% is how many the player owns
    - "&fGives everyone online a"
    - "&a1.5x Sell Multiplier"
    - "&fDuration: &a1 Hour"
    - "&fYou have: &a%amount%"
    - "&e&oClick to activate!"
  position:
    page: 1 # Which menu page to show on; pages are defined in config.yml
    row: 2 # 1 to 6
    column: 2 # 1 to 9
```

## Internal placeholders

These placeholders are provided by Boosters and can be used in this booster's messages, lore, and bossbar:

| Placeholder | Value |
| --- | --- |
| `%amount%` | How many of this booster the player owns (for use in GUI lore) |
| `%activator%` | The player who activated the booster (for use in messages) |
| `%time%` | The time the booster is activated or incremented for |
| `%time_remaining%` | The time left on the booster (for use in the bossbar) |

:::tip Troubleshooting
- **Booster not showing in the GUI?** Check the `gui.position` `page`, `row`, and `column` are within range and not colliding with the config mask, and that the `page` exists in `config.yml`, then run `/boosters reload`.
- **Changes not taking effect?** You did not reload; run `/boosters reload` after editing any booster file.
- **Two boosters of the same type run at once?** Give them the same `category` so one queues behind the other.
- **Activating just extends an existing booster?** That is `merge-tag` working; give them different tags to run separately.
:::

<hr/>

## Where to go next

- **Default configs:** study the [bundled boosters](https://github.com/Auxilor/Boosters/tree/master/eco-core/core-plugin/src/main/resources/boosters) for real, working examples.
- **Community configs:** browse user-created boosters on [lrcdb](https://lrcdb.auxilor.io/).
- **Effects reference:** the [Configuring an Effect](https://plugins.auxilor.io/effects/configuring-an-effect) guide for everything the effects blocks can do.
- **Commands:** [Commands and Permissions](commands-and-permissions) for giving, activating, and cancelling boosters.