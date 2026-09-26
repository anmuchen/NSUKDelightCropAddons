# NSUK：乐事作物附属 — 1.21.1 开发环境源码

本目录是由编译产物 **反编译还原** 得到的 NeoForge 1.21.1 开发工程，可直接用 IDEA / VS Code / Eclipse 导入、编译与运行。

- 来源 jar：`NSUKDelightCropAddons-1.0.19-neoforge-1.21.1.jar`
- 反编译工具：Vineflower 1.11.1
- 许可证：GPL 3.0（见 `src/main/resources/LICENSE.txt`）

## 环境要求

| 项目 | 版本 |
| --- | --- |
| JDK | 21（toolchain 已声明，Gradle 可自动拉取） |
| Gradle | 8.14.2（随 `gradlew` 提供，下载源为腾讯云镜像） |
| NeoForge | 21.1.248 |
| Minecraft | 1.21.1 |
| 映射 | Parchment 1.21.1 / 2024.11.17 |

## 常用命令

```bash
./gradlew build          # 编译并打包 -> build/libs/nsukdelightcropaddons-1.0.19.jar
./gradlew compileJava    # 仅编译
./gradlew runClient      # 启动开发版客户端（已自动挂载 libs/ 前置）
./gradlew runServer      # 启动开发版服务端
./gradlew runData        # 数据生成
```

Windows 下用 `gradlew.bat` 替代 `./gradlew`。

## 目录结构

```
.
├── build.gradle                  # ModDevGradle 2.0.91 构建脚本
├── settings.gradle
├── gradle.properties             # 版本与元数据（值为文档性，真实元数据见 mods.toml）
├── gradlew / gradlew.bat
├── gradle/wrapper/               # Gradle 8.14.2（腾讯云镜像）
├── libs/                         # 编译期 / 运行期本地前置模组（未发布到公共 Maven）
│   ├── New-Sim-U-Kraft-2.2.0-neoforge-1.21.1.jar   # 宿主模组 simukraft，mixin 注入目标
│   ├── ldlib2-neoforge-1.21.1-2.2.40-all.jar       # GUI 工具库
│   ├── taffy-1.1.4.jar                             # LDLib2 嵌套布局引擎
│   └── yoga-1.0.0.jar                              # LDLib2 嵌套样式模型
└── src/main/
    ├── java/com/xiaolianganmuchen/nsukdelightcropaddons/
    │   ├── NSUKDelightCropAddons.java              # 主类 @Mod
    │   ├── NSUKDelightCropAddonsClient.java        # 客户端入口
    │   ├── api/          DelightCropApi
    │   ├── client/       DelightCropSelectScreen / DelightAddCropScreen / DelightModConfigScreen
    │   ├── config/       DelightCropConfig / CustomCropStorage / CropSelectUiStorage
    │   ├── crop/         CropCatalog / CropDiscovery / CropHarvest / CropJsonReloadListener …
    │   ├── mixin/        5 个通用 mixin + mixin/client/ 2 个客户端 mixin
    │   └── nsuk/         FarmlandBoxDataExt / FarmlandBoxDataAccess / FarmlandBoxReconcile …
    └── resources/
        ├── META-INF/neoforge.mods.toml             # 模组元数据（原样保留，含全部依赖声明）
        ├── nsukdelightcropaddons.mixins.json
        ├── nsukdelightcropaddons.client.mixins.json
        ├── assets/nsukdelightcropaddons/lang/      # en_us.json / zh_cn.json
        ├── data/nsukdelightcropaddons/nsuk_crops/  # 15 份乐事系列作物定义 JSON
        ├── logo.png
        └── LICENSE.txt
```

## 已验证

- `./gradlew build` **构建成功**，产出 `build/libs/nsukdelightcropaddons-1.0.19.jar`。
- 产出 jar 与原 jar 的**条目清单完全一致**：44 个 class、14 个 data JSON、2 个 mixin 配置、双语 lang、`logo.png`、`neoforge.mods.toml`，双向差异为零（`zh_cn.json` 二进制校验一致）。

## 反编译修正记录

反编译在 3 处丢失了源码层面的冗余信息，已按语义还原：

1. `config/DelightCropConfig.java` — `((List)DISABLED_CROPS.get())` 泛型被擦除，还原为 `((List<String>)DISABLED_CROPS.get())`。
2. `mixin/FarmlandBoxDataMixin.java`、`nsuk/FarmlandBoxDataAccess.java` — 目标类 `FarmlandBoxData` 是 `final class`，鸭子类型接口强转必须经 `Object` 中转，还原为 `(FarmlandBoxDataExt)(Object)data`（该中转不产生字节码，故反编译器将其合并）。

另将 `FarmlandBoxDataExt` 中被命名为 `var1` 的形参还原为 `id`。

## 注意事项

- 这是**反编译还原**的源码，而非作者原始工程：方法与字段名、lambda 拆分方式、局部变量名可能与原作有差异；反编译器会把 `original.call(a, b, c)` 这类可变参数调用输出为 `original.call(new Object[]{a, b, c})`，语义等价但写法不同。
- MixinExtras（`@WrapMethod` / `Operation`）由 NeoForge 21.1.x 隐式提供，无需在 `build.gradle` 中显式声明。
- `libs/` 中的前置仅作本地编译/调试依赖，打包时**不会**被并入产物，与原 jar 行为一致。
- Mixin 配置未使用 refmap —— NeoForge 1.21.1 运行于官方映射，与原 jar 一致。
- 使用与分发请遵守 GPL 3.0 及上游模组（simukraft、LDLib2 等）的授权条款。
