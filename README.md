# SlashBladeTetra

这是Minecraft 1.20.1 Forge模组的SlashBladeTetra仓库。

SlashBladeTetra是基于原版SlashBlade模组与Tetra模组的集成模组，提供完整的SlashBlade Tetra化体验。

## 核心功能

### 模块化SlashBlade
- 提供专用的**SlashBladeModularItem**（模组物品）
- 完全兼容Tetra模组的模块系统
- 支持Blade、Handle、Scabbard、Soul、Tsuba等槽位
- 支持Synergy（协同效果）和Stat Sorter
- 支持**注魂**（Soul Infusion）、**裹柄**（Handle Wrap）等Tetra特性
- 自动处理Tetra的耐久、属性、效果等

### 灵魂提取
- **普·萃魂**（Soul Extraction Schematic）：普通灵魂提取
- **魔·萃魂**（Enchanted Soul Extraction Schematic）：附魔灵魂提取
- 支持多级注魂（最高注魂 VI）

### 交换系统
- **拔刀模块转移结晶**（Modular Exchange Item）
- 支持Blade、Handle、Scabbard等部件的交换
- 保留原SlashBlade的BladeState属性

### 其他特性
- JEI兼容（物品栏、合成配方等）
- 自定义Tooltip显示（耐久度、剑魂映射、精炼伤害等）
- 自定义GUI统计信息
- 兼容CialloBlade、ExtraHoloPage等模组
- 灵魂提取标签和替换机制

## 构建

需要 JDK 17（Gradle 自身用 JDK 21 启动，编译产物固定为 Java 17 字节码）。

```bash
# 构建全部三个模块（common + 每个 Tetra 版本各一个）
gradlew build

# 只构建某一个 Tetra 版本的 jar
gradlew :tetra-6.17.0:build      # 产物: tetra-6.17.0/build/libs/
gradlew :tetra-6.9.0:build       # 产物: tetra-6.9.0/build/libs/

# 离线构建（依赖已缓存时）
gradlew --offline build
```

Windows 的 MSYS/Git Bash 里 `./gradlew` 不可用（缺 `cygpath`），请用 `gradlew.bat`：

```bash
cmd //c "gradlew.bat --console=plain build"
```

### 项目结构

| 模块 | 内容 |
| --- | --- |
| `:common` | 全部共享代码、资源、mixin。只依赖最低支持版本的 Tetra API，保证向上兼容 |
| `:tetra-6.9.0` | 面向 Tetra 6.9.0 的版本差异实现（构建脚本 3 行） |
| `:tetra-6.17.0` | 面向 Tetra 6.17.0 的版本差异实现（构建脚本 3 行） |

版本差异全部收敛在 `:common` 的 `net.yiran.sbtetra.api` 接口之后，各版本模块用 ServiceLoader 注册自己的实现。

### 新增一个 Tetra 版本

1. 在 `settings.gradle.kts` 的 `include` 列表里加一行模块名。
2. 新建目录，放入 3 行 `build.gradle.kts`（只 apply `net.neoforged.moddev.legacyforge`）。
3. 放入 4 行 `gradle.properties`：`tetra_version`、`tetra_file_id`、`mutil_file_id`、`tetra_version_range`（必须写真实区间，裸版本号是软约束，会匹配任意更高版本）。

其余配置由根 `build.gradle.kts` 统一驱动，无需复制任何脚本。

## 报告Bug

如果有Bug，请加入QQ群：2157051200 拷打作者（不是

## 开源协议

本项目采用 [GNU Lesser General Public License v3.0](LICENSE.txt)（LGPL-3.0）协议开源。

Copyright (C) 2026 _yi_ran_
