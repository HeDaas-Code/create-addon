# CLAUDE.md — 项目级 Agent 协作规范

This document tells **Claude Code (CC)** how to work in this project.
**Read this before touching any code.** Hermes Agent (the user-side assistant) is the **project manager**; CC is the **lead engineer**.

---

## 0. Your role vs. Hermes Agent's role

| | Hermes Agent (in WeChat) | **You, Claude Code (CLI)** |
|---|---|---|
| Function | **Project manager** — translates user requests into tasks, dispatches work, verifies CI status, reports back to user | **Engineer** — writes code, runs builds, fixes bugs |
| Sees full context (this conversation, memory, prior sessions) | ✅ | ❌ — you only see what's in this repo |
| Loads matt-pocock engineering skills via Hermes' `matt-pocock-engineering/*` umbrella | ✅ | ❌ — you don't have these skills loaded |
| Has the user's intent in natural language | ✅ | ❌ — you only see concrete task instructions |
| Encodes the workflow you should follow | via this file | this file IS the encoding |

**Practical rule**: when the user asks for a feature, Hermes dispatches you with a concrete task spec + relevant pointers into this file. You execute. You do NOT do scope-exploration or feature-design — those are already done.

---

## 1. Engineering workflow — the matt-pocock stages, mapped to your tooling

Hermes dispatches tasks in **stages**. Each stage maps to a CC-native approach + a deliverable. **The full upstream umbrella** is at https://github.com/mattpocock/skills (Hermes-side mirror). You only need to follow the *deliverables* below; you do not load those skills yourself.

| matt-pocock stage | What you do (CC) | Deliverable |
|---|---|---|
| **grill-me** | (Hermes does this — don't run yourself) | n/a — you receive an already-grilled spec |
| **to-prd** | (Hermes does this — you receive the PRD as your task spec) | n/a |
| **to-issues** | (Hermes splits into issues; you receive a single concrete issue) | n/a |
| **triage** | (Hermes triages; you receive priority + type) | n/a |
| **prototype** | Throwaway spike to validate an API/approach — branch `prototype/<topic>`, no CI gate, README explains what was learned | README.md in the prototype dir |
| **tdd** | **Default mode for new features.** Red→green→refactor. Tests-first. | New/updated tests + green `./gradlew build` |
| **diagnose** | 4-phase: reproduce → minimize → hypothesize → instrument → fix. **Use when fixing bugs.** | Root-cause commit with failing→passing test |
| **improve-codebase-architecture** | (Hermes may dispatch you for this on occasion) | Refactor PR with the original tests still green |
| **grill-with-docs** | (Hermes does this — you only need to keep `CONTEXT.md` + `docs/adr/` up to date if asked) | Update CONTRIBUTING.md / docs/adr/<NNNN>-*.md |
| **handoff** | (Hermes does this — you do not) | n/a |

**Default workflow on every feature/bugfix task**: receive task → **tdd** (write failing test → make it green → refactor) or **diagnose** (repro bug → minimize → root-cause → fix). No skipping. **Tests are non-negotiable.**

---

## 2. Code indexing — codegraph is mandatory

For every task that touches Create / NeoForge / MC APIs, you **must consult the codegraph index first** before writing code. This avoids hallucinating API signatures (a recurring failure mode — see `~/.hermes/skills/game-modding/create-addon-dev/references/verified-api-signatures.md` for the error trail).

### 2.1. When to consult codegraph

- ✅ Touching `com.simibubi.create.*` (Create internals)
- ✅ Touching `net.neoforged.*` (NeoForge internals)
- ✅ Subclassing `KineticBlockEntity` / `ProcessingRecipe` / `BlockEntityBehaviour`
- ✅ Calling `Registrate` / `CreateRegistrate` fluent chains
- ✅ Wiring up mixin or accessor
- ❌ NOT needed for: vanilla MC API (`net.minecraft.*`), standard Java, project-local code

### 2.2. How to consult codegraph

The index is built from two repos cloned into the project workspace:

| Repo | Path | Why |
|---|---|---|
| `Creators-of-Create/Create` | `~/Project/MC_MOD_DEV/.codegraph/create/` | All Create 6.0.10 internals (kinetics, processing recipes, behaviours, mixins) |
| `NeoForge-Mods/NeoForge` (or `neoforged/NeoForge`) | `~/Project/MC_MOD_DEV/.codegraph/neoforge/` | NeoForge 21.1.219 internals (mod loader, event bus, AT, datagen) |

**Setup once** (if not already present):

```bash
mkdir -p ~/Project/MC_MOD_DEV/.codegraph
cd ~/Project/MC_MOD_DEV/.codegraph
git clone --depth 1 --branch mc1.21.1-6.0.10 https://github.com/Creators-of-Create/Create.git create 2>&1 | tail -3
git clone --depth 1 --branch 21.1.x https://github.com/neoforged/NeoForge.git neoforge 2>&1 | tail -3
cd create && codegraph init && cd ../neoforge && codegraph init
```

**Per-task lookup** (run BEFORE writing code):

```bash
# Find a class/interface
codegraph_search "ShellSubLevelImpactCallback" --kind class --path ~/Project/MC_MOD_DEV/.codegraph/create/

# Read the exact signature of a method
codegraph_node --file "src/main/java/com/simibubi/create/content/processing/recipe/ProcessingRecipe.java" --line 50

# Cross-repo: how does NeoForge resolve DeferredRegister bound to mod bus
codegraph_node --file "src/main/java/net/neoforged/neoforge/registries/DeferredRegister.java" --line 80
```

**Rule**: if you find yourself about to write `import com.simibubi.create.X.Y.Z` and you have not run `codegraph_search` for `X.Y.Z` in this session, **stop and run it first**. Verify the class actually exists at the version you're targeting. This is the #1 cause of "looks-right code that won't compile."

### 2.3. Cross-repo bug diagnosis (CBC × Sable pattern)

When a bug spans two repos (e.g. "mod A crashes against mod B"):

1. `codegraph_search` in repo A for the crash class → read the class
2. `codegraph_search` in repo B for the interface whose method is unresolved → read the interface
3. `git log -- <file>` in repo A → check if fix exists in source
4. Compare fix commit date vs modrinth release date → "fix exists but not published yet" is a common outcome

Document this in the bug-fix commit message. The pattern is encoded in `mc-mod-compat` skill Pitfall 21.

---

## 3. Build & test commands (verified 2026-06-26)

```bash
# Local build (cache hit ~10s; cold ~3min)
gradle build --no-daemon --stacktrace

# Run the full test suite (currently empty — write tests as you add features)
gradle test --no-daemon

# Datagen (only if you added blockstate provider classes)
gradle runData

# Spot-check the jar's metadata
unzip -p build/libs/create_addon-1.21.1-*.jar META-INF/neoforge.mods.toml
```

**CI runs `./gradlew build --no-daemon --stacktrace`** on every push to `main` / `master` / `dev` / `feature/**` / `fix/**`, every PR, weekly Mon 08:00 UTC, and on manual dispatch. The `build.yml` asserts that the packaged jar's `META-INF/neoforge.mods.toml` declares `create` + `neoforge` deps.

**You MUST keep CI green.** If you push and CI fails, fix it. Do not merge PRs with red CI.

---

## 4. Stack pins (do not bump without justification)

`gradle.properties` is the single source of truth:

| | Version | Comment |
|---|---|---|
| `minecraft_version` | 1.21.1 | matches user's pack |
| `neo_version` | 21.1.219 | NeoForge for 1.21.1 |
| `create_version` | 6.0.10-280 | Create 6.0.10 |
| `ponder_version` | 1.0.82 | Ponder docs |
| `flywheel_version` | 1.0.6 | NOT currently a dep in this template |
| `registrate_version` | MC1.21-1.3.0+67 | jarJar-ed |

**When bumping any version**: re-verify `./gradlew build` passes locally first, then update `gradle.properties`, then push. CI will catch upstream API drift.

---

## 5. Project layout (don't restructure without asking)

```
src/main/java/com/example/createaddon/
├── CreateAddonTemplate.java         # @Mod entry point — DO NOT rename MOD_ID without repo coordination
├── content/
│   ├── blocks/                       # Block + BlockEntity implementations
│   └── recipes/                      # Recipe implementations (currently vanilla, not ProcessingRecipe)
└── registry/                         # ModBlocks, ModBlockEntities, ModItems, ModRecipes
                                      # Each wires Registrate DeferredRegisters to modBus
```

**Add new features under the matching package**: a new machine block → `content/blocks/` + register in `registry/ModBlocks.java`. A new item → `registry/ModItems.java`. A new recipe type → `content/recipes/` + register in `registry/ModRecipes.java`.

**DO NOT**:
- rename `MOD_ID` (will break every downstream player's install)
- move `CreateAddonTemplate.java` (CI assertions depend on the package layout)
- delete `ModBlocks.java` etc. (add new entries; never remove the registration call site)
- touch `.github/workflows/build.yml` without explicit Hermes instruction (CI gates depend on it)

---

## 6. Git workflow

- **Default branch**: `main`. Feature branches: `feature/<topic>`. Bug branches: `fix/<topic>`.
- **Commit style**: imperative subject, blank line, body explaining *why* (not *what*). Match the existing commits in `git log`.
- **One commit per logical change**. Tests + code in the same commit.
- **No force-pushes to `main`**. Force-push on `feature/*` is fine before review.
- **PR description must include**: what changed, why, what tests were added, what CI run verifies it.
- **Do not commit** `build/`, `.gradle/`, `*.jar` (already in `.gitignore`).

---

## 7. Known pitfalls (don't repeat)

Encoded in `~/.hermes/skills/game-modding/create-addon-dev/SKILL.md` — read this file in full before starting any task. Top gotchas:

- **Recipe 1.21 API**: `Recipe<RecipeInput>` not `Recipe<Container>`; `getResultItem(Provider)` not `getResultItem(RegistryAccess)`.
- **No `Registrate.recipeType/recipeSerializer` helpers** — use vanilla `DeferredRegister<RecipeType<?>>`.
- **No `BlockStateGen.axial()` or `axisBlockProvider()`** for the worked example — drop `.transform(...)` entirely.
- **Groovy DSL**: `transitive = false` not `isTransitive = false`.
- **Flywheel**: artifact only on `maven.createmod.net`, NOT `maven.neoforged.net`.
- **ProcessingRecipe 6.0.6 refactor**: factory + codec + streamCodec wiring is deep — only migrate when you actually need Create machines to accept your recipes (see the migration reference in the skill).

---

## 8. When you're done with a task

1. `./gradlew build --no-daemon --stacktrace` — verify green locally
2. `git diff --stat` — confirm scope matches the task
3. `git commit` with the standard style
4. `git push origin <branch>` — trigger CI
5. Wait for CI to complete before declaring done
6. Report back to Hermes with: branch name, commit SHA, CI run URL, list of files changed, summary of what was done

**Hermes will verify CI status and report to the user.** Do not declare "done" without a green CI run.

---

## 9. Questions or ambiguous task specs

If a dispatched task is ambiguous, **ask Hermes** (write a comment in your final report and exit). Do NOT scope-creep into design decisions; that's Hermes' job (grill-me + to-prd stages). If a task references a class/method that doesn't exist in the indexed codegraph, **stop and report** — don't invent the signature.