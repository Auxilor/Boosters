---
title: "Commands and Permissions"
sidebar_position: 4
---

Every Boosters command and the permission node it requires. Players need `boosters.command.boosters` to open the menu; the rest are admin actions you can grant selectively.

| Command                                                                     | Description                                              | Permission                  |
|-----------------------------------------------------------------------------|----------------------------------------------------------|-----------------------------|
| `/boosters`                                                                 | Opens the boosters menu                                  | `boosters.command.boosters` |
| `/boosters activate <booster>`                                              | Activate a booster                                       | `boosters.command.activate` |
| `/boosters bossbar`                                                         | Toggles the bossbar visibility on/off for the player     | `boosters.command.bossbar`  |
| `/boosters cancel <all/booster/category> <category_id/booster_id> [silent]` | Cancel the active boosters (Silent skips expiry-effects) | `boosters.command.cancel`   |
| `/boosters give <player> <booster> [amount]`                                | Give a player a booster                                  | `boosters.command.give`     |
| `/boosters queue <category_id>`                                             | Shows the current booster queue for the category         | `boosters.command.queue`    |
| `/boosters reload`                                                          | Reload the plugin                                        | `boosters.command.reload`   |

<hr/>

## Where to go next

- **Build a booster:** [How to Make a Booster](how-to-make-a-custom-booster) to create boosters to give and activate.
- **Plugin config:** [Plugin Config](plugin-config) for the GUI and sound settings.
