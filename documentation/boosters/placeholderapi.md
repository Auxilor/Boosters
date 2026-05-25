---
title: "PlaceholderAPI"
sidebar_position: 3
---

| Placeholder                              | Description                                                                                                                                                                                                                     |
|------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `%boosters_<booster>_info%`              | If the booster is active, it will display the name of the booster and the player who activated it. If not, it will show a message showing that the booster is not active. The exact messages shown are configurable in lang.yml |
| `%boosters_<booster>_active_name%`       | Shows the name of the active booster, or an empty string if the booster is not active                                                                                                                                           |
| `%boosters_<booster>_name%`              | Shows the name of the booster                                                                                                                                                                                                   |
| `%boosters_<booster>_player%`            | Shows the display name of the player who activated the current booster, or an empty string if the booster is not active                                                                                                         |
| `%boosters_<booster>_seconds_remaining%` | Shows the amount of seconds left on a booster, or zero if the booster is not active                                                                                                                                             |
| `%boosters_<booster>_time_remaining%`    | Shows the amount of time left on a booster, formatted as hh:mm:ss (ie 01:05:12). Shows 00:00:00 if the booster is not active                                                                                                    |
| `%boosters_active_list%`                 | Shows a list of names of active boosters separated by comma, or message showing that there isn't any active booster. Message is configurable in lang.yml                                                                        |
| `%boosters_active_ids_list%`             | Shows a list of IDs of active boosters separated by comma, or message showing that there isn't any active booster. Message is configurable in lang.yml                                                                          |
