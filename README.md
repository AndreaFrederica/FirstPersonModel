# First-person Model · 1.12.2

[![Build](https://github.com/AndreaFrederica/FirstPersonModel/actions/workflows/build.yml/badge.svg?branch=codex%2Ffirstperson-1.12.2)](https://github.com/AndreaFrederica/FirstPersonModel/actions/workflows/build.yml)
[![Release](https://github.com/AndreaFrederica/FirstPersonModel/actions/workflows/release.yml/badge.svg)](https://github.com/AndreaFrederica/FirstPersonModel/actions/workflows/release.yml)

将 [tr7zw/FirstPersonModel](https://github.com/tr7zw/FirstPersonModel) 移植到 Minecraft 1.12.2，让你在第一人称低头时看到自己的身体。沿用原版玩家动画，支持皮肤、装备和手持物品渲染。

本分支为 `codex/firstperson-1.12.2`，采用 NeoFontRender 的 Unimined / Cleanroom 构建方式，源码位于标准的 `src/main` 目录。

## 安装与使用

需要 **Minecraft 1.12.2、Cleanroom 0.6.13-alpha、Java 25**。这不是适用于原版 Forge / Java 8 的构建。

从 [GitHub Releases](https://github.com/AndreaFrederica/FirstPersonModel/releases) 下载 `firstperson-1.12.2-<版本>.jar`，放入客户端 `mods` 目录。不要安装 `-sources.jar` 或开发用的 `-dev.jar`。

仅客户端需要安装；服务端无需安装。Mixin 加载能力由 Cleanroom 提供，不需要另外安装动画模组。目前只通过 GitHub 发布，CurseForge 项目尚未申请。

- **F6**：启用或关闭第一人称身体。
- **Mods → First-person Model → Config**：打开配置界面，也可编辑 `config/firstperson.cfg`。
- 支持站立、潜行、乘骑的身体偏移配置。
- 手部模式：`OFF`（模型手臂）、`ITEMS`（持物时使用原版手部）、`ALL`（原版手部）、`ALL_DOUBLE`（额外显示空副手）。
- 支持动态手部切换、指定物品使用原版手部、指定物品暂时关闭身体，以及隐藏指定渲染层。地图默认使用原版手部。
- 睡眠、鞘翅飞行、旁观者和脱离玩家的摄像机会暂时恢复原版渲染。

第一人称渲染阶段会隐藏头部、帽子、头盔，以及重复的手臂和物品；不会修改玩家实体坐标或永久改变模型部件的可见性。

## 本地构建

安装 JDK 25 并配置 `JAVA_HOME`，然后执行：

```powershell
.\gradlew.bat clean build
```

Linux / macOS：

```sh
chmod +x gradlew
./gradlew clean build
```

产物位于 `build/libs/`。运行 `python tools/verify_artifact.py` 可以检查发布包的元数据、Mixin 生产映射、语言文件和许可证。

开发客户端使用 `gradlew.bat runClient`。如果 Gradle 枚举磁盘时卡住，追加 `--no-watch-fs`。

## GitHub Actions

### 构建

[Build 1.12.2](.github/workflows/build.yml) 在分支 push、Pull Request 和手动触发时执行：

1. 安装 JDK 25，配置 Gradle 缓存。
2. 执行 `clean build` 和发布包校验。
3. 上传可安装 JAR 和源码 JAR 到 Actions Artifacts，不上传开发 JAR。

### 发布到 GitHub Releases

[Release 1.12.2](.github/workflows/release.yml) 在推送 `v*` 标签时自动构建、校验并发布 GitHub Release。只需要仓库自带的 `GITHUB_TOKEN`，发布任务已声明 `contents: write` 权限，无需另外配置个人令牌。

发布前同步更新：

- `gradle.properties` 中的 `mod_version`。
- `FirstPersonModelMod.java` 中的 `@Mod` 版本。
- `CHANGELOG.md` 中对应版本的更新说明。

提交并推送代码，再创建对应标签。例如当前版本：

```sh
git push origin codex/firstperson-1.12.2
git tag v2.7.3-backport.1
git push origin v2.7.3-backport.1
```

标签必须为 `v` 加 `mod_version`。工作流会校验标签指向当前构建提交、模组版本一致且存在更新说明，不匹配时停止发布。GitHub Release 附带可安装 JAR、源码 JAR及更新说明。

也可以在 Actions 的 **Release 1.12.2 → Run workflow** 中输入已存在的标签，并选择 `release`、`beta` 或 `alpha`；后两者会标记为 GitHub 预发布。标签 push 默认使用 `release`。GitHub 的手动运行入口需要该工作流已存在于仓库默认分支。

### 未来启用 CurseForge

CurseForge 发布任务默认跳过，不影响 GitHub Release，也不需要提前设置 CF 密钥。申请本移植项目并获批后，在仓库 **Settings → Secrets and variables → Actions** 配置：

| 类型 | 名称 | 值 |
| --- | --- | --- |
| Variable | `CURSEFORGE_PROJECT_ID` | 本移植项目自己的数字 ID，不要使用上游项目 ID |
| Secret | `CURSEFORGE_TOKEN` | 有上传权限的 CurseForge API Token |
| Variable | `ENABLE_CURSEFORGE` | `true`；最后设置，启用发布 |

启用后，每次发布先完成 GitHub Release，再上传同一构建的可安装 JAR 到 CurseForge，使用同一版本、发布渠道和更新说明。CF 上传失败不会撤回已经发布的 GitHub Release。禁用时删除 `ENABLE_CURSEFORGE` 或设为 `false`。

CF 文件标记 Minecraft `1.12.2` / Forge 分类（Cleanroom 基于 Forge），但仍然要求 **Cleanroom + Java 25**；项目页面也应明确标注。工作流不会创建或申请 CurseForge 项目。重新运行上传任务前应检查 CF 是否已有该文件，以免重复上传。

## 验证与兼容性

已通过构建、生产映射检查及实机的身体显示、F6 开关和 F5 视角切换检查，详见 [VALIDATION.md](VALIDATION.md)。

仍需在目标整合包中检查：潜行和乘骑、盔甲与附魔头盔、各手部模式、左手设置、地图/弓/盾/副手、物品栏预览、睡眠和鞘翅、重连，以及其他玩家的模型。第三方自定义模型、盔甲和光影兼容性尚未全面验证。

新版 Minecraft 的游泳/爬行动画、望远镜，以及现代 PlayerAnimator / Freecam 集成不包含在此移植版中。客户端扩展 API 保留启用条件和身体偏移处理器，使用 1.12.2 的 `AbstractClientPlayer` / `Vec3d` 类型。

## 致谢与许可证

原项目由 [tr7zw](https://github.com/tr7zw) 开发：[FirstPersonModel](https://github.com/tr7zw/FirstPersonModel)。构建参考 NeoFontRender。保留上游 [MIT 许可证](LICENSE)。
