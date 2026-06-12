---
title: "Plugin Config"
sidebar_position: 5
---

The plugin-wide settings live in `config.yml` in the Boosters data folder (`/plugins/Boosters/config.yml`). It controls storage, the `/boosters` GUI layout, and the sounds played on booster events. Edit it, then run `/boosters reload` to apply changes.

:::warning
Changing `use-local-storage` switches where booster data is read from, so re-log after reloading to make sure the player data you see is current.
:::

## Default config.yml

```yaml
# Force Boosters to save to local storage even when eco uses a database.
# Disables cross-server sync; leave false for networks.
use-local-storage: false

# Warn players before active boosters expire. Each value is the ticks remaining
# at which to warn (20 ticks = 1 second); a warning fires once as the remaining
# time crosses each threshold. The message lives in lang.yml (expiry-warning)
# and supports %booster% and %time% (HH:MM:SS). Boosters opt out with
# "expiry-warning: false".
expiry-warning-intervals:
  - 1200 # 60 seconds remaining
  - 600 # 30 seconds remaining
  - 200 # 10 seconds remaining

# true = action bar, false = chat
expiry-warning-action-bar: false

gui:
  title: Boosters # Title of the /boosters menu. Supports %page% and %max_page%
  rows: 3 # Menu height, 1 to 6 rows

  # Navigation arrows, automatically hidden on the first/last page
  forwards-arrow:
    item: arrow name:"&fNext Page"
    row: 3 # 1 to 6
    column: 6 # 1 to 9
  backwards-arrow:
    item: arrow name:"&fPrevious Page"
    row: 3
    column: 4

  # Each entry is one page of the menu. Append more to add pages.
  pages:
    - page: 1 # Page number boosters reference via their gui.position.page
      mask: # Background filler items, drawn behind boosters
        items:
          - black_stained_glass_pane # Item used for masked slots
        pattern: # 1 = masked, 0 = open slot a booster can occupy
          - "111111111"
          - "101101101"
          - "111111111"
      custom-slots: [] # Optional extra slots; see https://plugins.auxilor.io/all-plugins/custom-gui-slots

sounds:
  activate: # Played when a booster is activated
    enabled: true
    sound: ENTITY_PLAYER_LEVELUP # Any Bukkit Sound name
    volume: 2
    pitch: 0.9
    category: AMBIENT # Sound category the volume slider obeys
  increment: # Played when an active booster's duration is extended
    enabled: true
    sound: ENTITY_EXPERIENCE_ORB_PICKUP
    volume: 2
    pitch: 0.9
    category: AMBIENT
  expire: # Played when a booster ends
    enabled: true
    sound: ENTITY_ITEM_BREAK
    volume: 2
    pitch: 0.9
    category: AMBIENT
  expiry-warning: # Played alongside each expiry warning
    enabled: true
    sound: BLOCK_NOTE_BLOCK_PLING
    volume: 2
    pitch: 1.0
    category: AMBIENT
```

<hr/>

## Where to go next

- **Build a booster:** [How to Make a Booster](how-to-make-a-custom-booster) covers the per-booster config files.
- **Commands:** [Commands and Permissions](commands-and-permissions) for the reload and admin commands.