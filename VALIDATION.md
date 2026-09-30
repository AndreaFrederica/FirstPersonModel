# 1.12.2 backport validation

Target: Minecraft 1.12.2, Cleanroom 0.6.13-alpha, Zulu JDK 25.0.3.
Branch: `1.12.2`.

Completed on 2026-10-01:

- `gradlew.bat --no-watch-fs build`: successful compilation, resources, sources
  JAR and MCP-to-SRG production remapping.
- `python tools/verify_artifact.py`: checked installable JAR metadata, coremod
  manifest, all seven Mixin refmap entries, language resources and license.
- `gradlew.bat --no-watch-fs runClient`: client initialization and integrated
  single-player world entry succeeded. First-person Model was present in the
  loaded-mod list; no `InvalidMixin`, `InjectionError` or `MixinApplyError` appeared.
- Visual check in the test world: looking down displayed the slim-skin body and
  arms without the head. F6 removed the body and restored the vanilla first-person
  arm; F6 again restored the body. Both F5 third-person perspectives retained the
  head, and cycling back restored first-person rendering.

Compatibility update (2026-10-01):

- Eight JUnit tests cover optional APIs absent, Aqua swimming/crawling/exit states,
  partial-tick sampling, independent UIE camera/zoom states and hand-mode transitions.
- Hydro Agility 1.15.4 and First-person Model loaded together and entered the
  integrated test world. The downloaded Hydro Agility test JAR needed a local-only
  development mapping correction: its BiomeColorHelperMixin uses the literal SRG
  selector `func_180283_a` with remapping disabled; the MCP development runtime
  requires `getColorAtPos`. Only that selector in the ignored local test JAR was
  changed. No Hydro Agility code is bundled in this project or release artifact.
- The user tested swimming and confirmed it works. Automated UI testing was
  stopped by the user; crawling visuals and fishing-line alignment are not yet
  confirmed in-game.
- HGW spyglass IDs were checked against UIE's compatibility data. UIE integration
  follows its public CameraApi and ZoomModule interfaces and is tested with mock
  APIs; an actual UIE/spyglass joint runtime has not been verified.

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
- GitHub Release `v2.7.3-backport.1` was published. This compatibility update is
  unreleased; branch pushes run the build workflow, while releases require a tag.
  CurseForge remains disabled unless `ENABLE_CURSEFORGE` is set to `true`.
