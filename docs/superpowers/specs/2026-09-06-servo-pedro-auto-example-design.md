# Servo + Pedro Pathing Auto 安全占位示例设计

## 目标

为 RookieBot 增加一套只用于教学的 Auto 示例，展示：

- 如何封装 `HardwareMap` 中的 Servo；
- 如何创建 Pedro Pathing `Follower`；
- 如何声明起点与路径；
- 如何在 iterative OpMode 中调用 `followPath()`、`update()`、`isBusy()` 和 `breakFollowing()`；
- 如何用非阻塞状态机组合“关闭 Servo → 行驶 → 打开 Servo → 等待 → 行驶 → 结束”。

示例只说明调用方法和代码结构，不包含任何可直接用于实车的配置。

## 非目标

- 不提供 RookieBot 当前机器人的硬件名称、Servo 端点、Pose、马达方向或定位参数。
- 不复制 FTC16093 的 PID、Pinpoint offset、路径坐标或机构参数。
- 不引入 FTCLib Command、Road Runner、Dashboard 或 Panels。
- 不声明示例已经通过实车测试。
- 不自动部署到 Control Hub。

## 参考基线

- 当前项目继续使用 FIRST FTC SDK 11.2.1。
- Pedro Pathing 使用 Knowledge Bank 已做核心编译验证的 `com.pedropathing:ftc:2.1.2` API。
- FTC16093 仓库只用于参考其“Subsystem 封装机构、Auto 调用机构”的组织方式；其 Pedro 1.0.9 API 与机器人专属参数不进入本项目。

## 文件结构

```text
TeamCode/src/main/java/org/firstinspires/ftc/teamcode/
├── examples/
│   ├── autonomous/
│   │   └── ServoPedroAutoExample.java
│   └── hardware/
│       └── ExampleServoHardware.java
└── pedroPathing/
    └── Constants.java
```

此外修改根目录 `build.dependencies.gradle`，加入 Pedro Maven repository 和 Pedro 2.1.2 核心依赖。

## Hardware 封装

`ExampleServoHardware` 只负责 Servo 资源与机构语义，不负责 Auto 流程：

- 构造函数通过传入的 hardware name 执行 `hardwareMap.get(Servo.class,name)`；
- 保存 closed/open 两个占位位置；
- 构造时检查名称非空、位置有限且位于 `[0,1]`、两个位置不相同；
- 对外只暴露 `close()`、`open()` 等语义方法；
- 实际 `Servo` 字段保持 private，Auto 不直接调用 `setPosition()`。

这样新人可以看到清晰边界：OpMode 决定什么时候动作，Hardware wrapper 决定怎样向硬件发送动作。

## Pedro 封装

`pedroPathing/Constants.java` 只展示 `Constants.createFollower(hardwareMap)` 工厂入口。它使用 Pedro 2.1.2 的 `FollowerBuilder`，并以注释明确说明：

- 文件中的默认构造只用于编译和结构演示；
- 在允许实车行驶前，必须用 Pedro Quickstart 生成并在当前机器人上完成 drivetrain/localizer 配置与调参；
- 不得把 FTC16093 或其他机器人的常量复制为已验证配置。

Auto 只依赖这个工厂方法，不在比赛流程中散落 drivetrain/localizer 构造细节。

## Auto 安全模型

`ServoPedroAutoExample` 是 iterative `OpMode`，并同时采用两层锁定：

1. 文件保留 `@Disabled`；注释明确写明：只有示例文件使用 `@Disabled`，队员编写自己的 Auto 时不能添加或保留它，否则 Driver Station 不显示该 OpMode。
2. `CONFIGURATION_COMPLETE=false`；所有 hardware name、Servo 位置、Pose 和等待时间使用明显 sentinel，占位未替换时 INIT 只显示 telemetry，不构造 Follower、不取得 Servo，也不输出运动命令。

该设计使新人误删 `@Disabled` 后仍不会因占位参数而启动机构或底盘。

## Auto 调用流程

状态机使用具名 enum：

```text
PRELOAD_CLOSED
  → DRIVE_TO_ACTION
  → OPEN_SERVO
  → WAIT_FOR_SERVO
  → DRIVE_TO_PARK
  → DONE
```

- `init()`：验证占位配置；验证通过后创建 hardware wrapper、Follower 和 PathChain。
- `start()`：调用 `robot.close()`，再调用 `follower.followPath(...)`。
- `loop()`：每轮先调用 `follower.update()`；通过 `!follower.isBusy()` 推进路径状态；通过 `ElapsedTime` 完成非阻塞等待。
- `stop()`：先锁定状态，再 best-effort 调用 `follower.breakFollowing()`。

禁止使用 `sleep()` 或在一个 while 循环里阻塞等待路径完成，因为这会隐藏 FTC iterative OpMode 的正确更新结构。

## 示例中必须讲清的最基本调用

代码注释应在调用点解释以下方法：

- `hardwareMap.get(Servo.class,name)`：按照 Robot Configuration 名称取得 Servo；
- `Constants.createFollower(hardwareMap)`：集中创建 Pedro Follower；
- `follower.setStartingPose(startPose)`：声明初始化时机器人的真实场地 Pose；
- `follower.pathBuilder()` / `addPath(...)` / `setLinearHeadingInterpolation(...)` / `build()`：创建直线路径；
- `follower.followPath(path,maxPower,false)`：开始非阻塞路径跟随；
- `follower.update()`：每个 `loop()` 必须持续调用；
- `follower.isBusy()`：判断当前路径是否结束；
- `follower.breakFollowing()`：STOP 时尽力取消跟随；
- `robot.close()` / `robot.open()`：Auto 只通过封装后的语义方法控制 Servo。

## 错误处理与 telemetry

配置校验失败时，示例进入安全锁定并显示具体原因。运行时异常也进入安全停止，不继续推进状态。Telemetry 至少包括：

- `configuration complete`
- `safety locked`
- `auto state`
- `runtime failure`
- `follower busy`
- `x (in)`、`y (in)`、`heading (rad)`（Follower 可用时）
- `last servo command`
- `state elapsed (s)`

`last servo command` 表示最后一次成功发送的命令，不表示 Servo 有位置反馈。

## 验证边界

软件验证包括：

1. Gradle Sync 能解析 Pedro 2.1.2；
2. `:TeamCode:assembleDebug` 成功；
3. 静态检查确认示例带 `@Disabled`、默认 `CONFIGURATION_COMPLETE=false`，且没有 FTC16093 的机器人专属数值；
4. 静态检查确认 `loop()` 调用 `follower.update()`，状态推进依赖 `isBusy()`，Servo 由 hardware wrapper 控制。

软件构建成功不等于实车验证。任何人复制模板后，都必须依次验证 Robot Configuration、Servo 单独动作、Localization Test、低功率短路径，最后才允许组合 Auto。
