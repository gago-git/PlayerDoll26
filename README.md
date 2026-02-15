# PlayerDoll26
Simple Standalone Fake Player Plugin for Spigot, Paper, Folia 1.21 [Java 21+]

[Modrinth](https://modrinth.com/plugin/playerdoll26)

Release available on Modrinth.

Detailed usage has been migrated to [Wiki](https://github.com/sjavi4/PlayerDoll/wiki).

## Fork Notice

This repository is a maintained fork of the original project:

- Original project: https://github.com/sjavi4/PlayerDoll
- Original author: `sjavi4`
- Fork maintainer: `gago-git`

Attribution and publication notes are available in:

- `NOTICE.md` / `NOTICE.en.md`
- `CREDITS.md` / `CREDITS.en.md`

## Fork Scope (This Repository)

This fork focuses on maintenance and compatibility for the Minecraft 1.21 line.

- Supported range in this fork: **1.21.0 through 1.21.11**
- Current project version in this fork: **3.0**

### Version modules in this fork

- `Addon-Doll-v1_21_0-1_21_1`
- `Addon-Doll-v1_21_2-1_21_3`
- `Addon-Doll-v1_21_4`
- `Addon-Doll-v1_21_5`
- `Addon-Doll-v1_21_6A`
- `Addon-Doll-v1_21_6P`
- `Addon-Doll-v1_21_7-1_21_8`
- `Addon-Doll-v1_21_9`
- `Addon-Doll-v1_21_10`
- `Addon-Doll-v1_21_11`

Wrapper modules:

- `Addon-Wrapper-1_20_2-1_21_1`
- `Addon-Wrapper-1_21_2-1_21_4`
- `Addon-Wrapper-1_21_5`
- `Addon-Wrapper-1_21_6`
- `Addon-Wrapper-1_21_7_1_21_8`
- `Addon-Wrapper-1_21_9`
- `Addon-Wrapper-1_21_10`
- `Addon-Wrapper-1_21_11`

## Disclaimer

**This plugin is highly dependent on NMS. Small changes made by Mojang / server jar providers might cause this plugin to stop working. Please use it with caution.**

Features provided by this plugin are not guaranteed to be fully working, and it is not encouraged to treat this plugin as a paid feature to other players.

## How to Use

1. Putin your plugins/ folder.
2. Put the addon jars in the plugin addon folder (or the location your build already uses for addon loading).
3. For each server version, use 1 matching Addon-Doll + 1 matching Addon-Wrapper.
4. Start the server and confirm both addons were loaded in console (Addon Loaded).
5. Create and spawn a doll:
6. /dollmanage create (name)
7. /dollmanage spawn (name)
8. Configure behavior with /playerdoll:doll menu <name> or /dollmanage set ....

Quick mapping:

1. 1.21.0–1.21.1: Addon-Doll-v1_21_0-1_21_1 + Addon-Wrapper-1_20_2-1_21_1
2. 1.21.2–1.21.4: Addon-Doll-v1_21_2-1_21_3 or Addon-Doll-v1_21_4 + Addon-Wrapper-1_21_2-1_21_4
3. 1.21.5: Addon-Doll-v1_21_5 + Addon-Wrapper-1_21_5
4. 1.21.6: Addon-Doll-v1_21_6A (Spigot) or Addon-Doll-v1_21_6P (Paper) + Addon-Wrapper-1_21_6
5. 1.21.7–1.21.8: Addon-Doll-v1_21_7-1_21_8 + Addon-Wrapper-1_21_7_1_21_8
6. 1.21.9: Addon-Doll-v1_21_9 + Addon-Wrapper-1_21_9
7. 1.21.10: Addon-Doll-v1_21_10 + Addon-Wrapper-1_21_10
8. 1.21.11: Addon-Doll-v1_21_11 + Addon-Wrapper-1_21_11


### Upgrade from old version

1. Back up and delete old configs (except doll configs).
2. Start the server to regenerate new configs (with comments/usages).
3. Copy old settings to the new config keys according to their usage.

