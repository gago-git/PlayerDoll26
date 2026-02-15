# PlayerDoll
Simple Standalone Fake Player Plugin for Spigot, Paper, Folia 1.20.2+ [Java 17+]

[Modrinth](https://modrinth.com/plugin/playerdoll)

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

### Upgrade from old version (v1.28+)

1. Back up and delete old configs (except doll configs).
2. Start the server to regenerate new configs (with comments/usages).
3. Copy old settings to the new config keys according to their usage.

### In game (v1.28+)

1. Players without permission will not see corresponding commands/arguments.
2. Main commands:
   - `doll` or `playerdoll:doll`
   - `dollmanage` or `playerdoll:dollmanage` (same command family, bypasses some optional checks; OP required by default)
3. Create a doll:
   - `/doll create <name> [skin]`
4. Spawn a doll:
   - `/doll spawn <name>`
5. Most doll data changes require the doll to be online.
   - Exception: `Remove`
   - `/doll set` for doll-specific settings
   - `/doll gset` for global settings (all players)
   - `/doll pset` for player-specific overrides

## Build

```bash
mvn clean package -DskipTests
```

