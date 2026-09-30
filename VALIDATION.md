# 1.12.2 backport validation

Target: Minecraft 1.12.2, Cleanroom 0.6.13-alpha, Zulu JDK 25.0.3.
Branch: `codex/firstperson-1.12.2`.

Completed on 2026-10-01:

- `gradlew.bat --no-watch-fs build`: successful compilation, resources, sources
  JAR and MCP-to-SRG production remapping.
- `python tools/verify_artifact.py`: checked installable JAR metadata, coremod
  manifest, all six Mixin refmap entries, language resources and license.
- `gradlew.bat --no-watch-fs runClient`: client initialization and integrated
  single-player world entry succeeded. First-person Model was present in the
  loaded-mod list; no `InvalidMixin`, `InjectionError` or `MixinApplyError` appeared.
- Visual check in the test world: looking down displayed the slim-skin body and
  arms without the head. F6 removed the body and restored the vanilla first-person
  arm; F6 again restored the body. Both F5 third-person perspectives retained the
  head, and cycling back restored first-person rendering.

The test client is left paused in its world under `run/client`.
This is not an exhaustive compatibility test: riding, armor, every hand mode,
custom model mods and shader mods still need the manual checks in README.md.
Stock Forge / Java 8 are not supported by this Cleanroom-template build.

Gradle's filesystem watcher stalled while enumerating drives in this environment;
`--no-watch-fs` resolved it. Development startup emitted Cleanroom's expected
missing binary-patch/signature and loose-directory Mixin-source warnings, but
completed loading and entered the world.

Release automation validation:

- `actionlint` 1.7.12 accepted both build and release workflows.
- A fresh `clean build` and packaged-artifact validation passed.
- In an isolated temporary Git repository, release metadata checks accepted the
  release/beta channels and rejected an invalid channel, a mismatched version tag,
  mismatched `@Mod` version, missing changelog entry and a checkout not at the tag.
- Remote GitHub/CurseForge uploads have not been executed. CurseForge remains
  disabled unless the repository variable `ENABLE_CURSEFORGE` is set to `true`.
