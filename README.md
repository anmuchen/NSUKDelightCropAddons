# 乐事系列：作物附属（2.x）

把各类乐事系模组的作物接入 **New: Sim-U-Kraft（simukraft）** 的农田盒，由雇农统一种植、收割与补种。

- 模组 ID：`nsukdelightcropaddons`
- 当前版本：2.1.0
- 运行环境：Minecraft 1.21.1 / NeoForge 21.1.228 – 21.1.x
- 前置：`nsukcropapi` 1.0.0+、`simukraft` 1.0+
- 许可证：GNU General Public License v3.0

---

## 与 1.0.19 的区别

1.0.19 是单体实现，自带作物注册表、作业流程与 mixin，直接引用 `simukraft` 内部类，因此与 simukraft 2.3.0 的包名变更绑定。

2.x 改为**只提供数据**：所有通用逻辑交给 `nsukcropapi`，本模组只负责引导与少量特殊作物注册。simukraft 的内部变动由 API 承担，本模组不再直接引用它的类。

| 维度 | 1.0.19 | 2.1.0 |
| --- | --- | --- |
| Java 源文件 | 36 个 | 2 个 |
| 作物定义 JSON | 14 份 | 29 份 |
| simukraft 包引用 | `common.cn.kafei.simukraft` | 无 |

1.0.19 的源码仍保留在本仓库的 `main` 分支，用于维护 2.2.x 及更早的整合包；2.x 的开发在 `2.x` 分支进行。

---

## 数据目录

```
src/main/resources/data/nsukdelightcropaddons/nsuk_crops/*.json
```

每个 JSON 对应一个上游模组，字段说明见 `nsukcropapi` 的 README。新增来源只需放一份 JSON，不必改 Java 代码。未安装的上游模组会被自动跳过，不影响启动。

当前覆盖 29 个来源、175 种作物。

---

## 构建

本工程是 ModDevGradle 工程，`libs/` 以 flatDir 方式提供编译所需的 `nsukcropapi` jar。

```bash
# Windows
gradlew.bat build

# Linux / macOS
./gradlew build
```

产物：`build/libs/nsukdelightcropaddons-2.1.0.jar`

`libs/nsukcropapi-1.0.0.jar` 是编译依赖，若要针对更新版本的 API 编译，替换该文件即可。

---

## 许可证

GNU General Public License v3.0，全文见 [LICENSE](LICENSE)。
