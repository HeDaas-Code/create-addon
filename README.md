# Create Addon

A Minecraft Java Edition mod that extends the [Create](https://www.curseforge.com/minecraft/mc-mods/create) mod.

## Stack

| | Version |
|---|---|
| Minecraft | 1.21.1 |
| Mod loader | NeoForge 21.1.219 |
| Create | 6.0.10-280 |
| Registrate | MC1.21-1.3.0+67 |
| Java | 21+ |

## Building locally

```bash
# JDK 21+ required (tested on OpenJDK 25)
gradle build --no-daemon
# → build/libs/create_addon-1.21.1-0.1.0.jar
```

First build pulls ~200MB (NeoForge + MC 1.21.1 client/server + Vineflower decompile); ~3 min.
Subsequent builds hit the Gradle build cache: ~8s.

## CI

GitHub Actions workflow at `.github/workflows/build.yml` runs on push / PR / weekly cron / manual dispatch.
Built jar is uploaded as the `mod-jar` artifact (30-day retention).

## What's in the worked example

- **`GrinderWheelBlock`** — a kinetic block that extends Create's `ShaftBlock`, registers with `SimpleKineticBlockEntity` so it joins the Kinetics stress/RPM network for free.
- **`CrushingRecipe`** — a vanilla `Recipe<RecipeInput>` (1 input → 1 output). Trades off against Create's `ProcessingRecipe` ecosystem; see the `create-addon-dev` skill for the migration path when adding a machine Create can accept.
- **`ModBlocks` / `ModBlockEntities` / `ModItems` / `ModRecipes`** — Registrate-flavoured registry classes.

## Layout

```
.
├── build.gradle                    # moddev plugin + Create + Registrate deps
├── settings.gradle                 # pluginManagement with createmod + ithundxr repos
├── gradle.properties               # version pins (single source of truth)
├── .github/workflows/build.yml     # CI: build → upload artifact → assert mods.toml
└── src/main/
    ├── java/com/example/createaddon/
    │   ├── CreateAddonTemplate.java       # @Mod entry point
    │   ├── content/blocks/GrinderWheelBlock.java
    │   ├── content/recipes/CrushingRecipe.java
    │   └── registry/Mod{Blocks,BlockEntities,Items,Recipes}.java
    └── resources/
        ├── META-INF/neoforge.mods.toml    # mod metadata + deps
        └── pack.mcmeta
```

## License

MIT.