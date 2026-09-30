# Changelog

## Unreleased

- Add optional Aqua Acrobatics / Hydro Agility swimming and crawling pose integration,
  animated swimming arms, camera clearance and configurable offsets.
- Suppress the body while using configured spyglasses, including HGW defaults.
- Respect UIE camera ownership (freecam/drone, free look, shoulder and render overrides)
  and optionally UIE zoom.
- Restore dynamic model-arm transitions and align fishing lines with the rendered body.
- Add compatibility and hand-policy tests, and document the renamed `1.12.2` branch.

Swimming was confirmed working by the user. Actual spyglass and UIE integration
still require in-game verification; see VALIDATION.md for test coverage.

## 2.7.3-backport.1

Requires Minecraft **1.12.2**, **Cleanroom 0.6.13-alpha** and **Java 25**.
Client-side only. This is not a stock Forge / Java 8 build.

- Backport first-person body rendering and hide the head, headwear and helmet.
- Add F6 toggle, vanilla/dynamic hand modes, item overrides and configurable offsets.
- Provide an in-game configuration screen and 1.12.2 language resources.
- Preserve activation and player-offset extension APIs with 1.12.2 types.
- Add automated builds and GitHub Releases; CurseForge publishing is opt-in.

Build, production Mixin mapping checks and basic in-game F6/F5 checks passed.
Custom player models, armor and shader compatibility are not exhaustively tested.
