# Servo + Pedro Auto 示例

所有硬件统一在 `org.firstinspires.ftc.teamcode.Hardwares` 中声明、查找和初始化。业务代码直接访问公开字段：

```java
Hardwares hardwares=new Hardwares(hardwareMap);
hardwares.motors.leftFront.setPower(power);
hardwares.servos.servo1.setPosition(Hardwares.servoAngleToPosition(angleDegrees));
hardwares.sensors.pinpoint.update();
```

上面演示独立的调用方法。Pedro 正在运行时，由 Follower 控制底盘并更新 Pinpoint，不要同时手动写底盘功率、更新或重置定位器。

## 舵机使用角度

业务变量统一填写 `0~360` 度，由 `Hardwares.servoAngleToPosition()` 转成 SDK 的 `0~1`：

```java
double angleDegrees=180; // 换算示范，实际目标角度按机构测量填写
hardwares.servos.servo1.setPosition(Hardwares.servoAngleToPosition(angleDegrees));
```

公式是 `angleDegrees/360.0`：`0→0`、`90→0.25`、`180→0.5`、`270→0.75`、`360→1`。360 对应满行程，不会绕回零；负数、超过 360、NaN 或无穷大会被拒绝。

这里按满行程 360 度约定进行映射，实际机械转角取决于舵机型号和设置；换算函数不会让原本只能转 180 度的舵机变成 360 度。所有舵机共用该函数，无需分别写转换代码。

## 增加硬件

例如新增 `motor1`，只在 `Hardwares.Motors` 中添加：

```java
public final DcMotorEx motor1;

// 放在 Motors 构造方法内，名字对应 Robot Configuration：
motor1=hardwareMap.get(DcMotorEx.class,"motor1");
motor1.setDirection(DcMotorSimple.Direction.FORWARD);
```

以后任何 Auto/TeleOp 都直接调用 `hardwares.motors.motor1.setPower(power)`。新增舵机放在 `Servos`，新增 IMU、距离传感器等放在 `Sensors`；不要在 OpMode 中另外声明设备字段或调用 `hardwareMap.get()`。

当前 `Servos` 已声明 `servo1`、`servo2`。第一个舵机的配置名还是待填写字符串，第二个要求配置名为 `Servo2`。完整初始化会查找两个设备：没有第二个舵机时，应同时移除它的声明和初始化；要控制第二个舵机就使用 `hardwares.servos.servo2`。

## 新手阅读与修改顺序

先读 `Hardwares` 的编号注释，了解设备从哪里来；再读 `Constants` 的参数说明，最后读 Auto 的 `init → start → loop → stop`。只学习摇杆驾驶时，可以直接读 `MecanumDrive` 的四个步骤。

| 想修改什么 | 修改位置和方法 |
| --- | --- |
| 增加电机、舵机或传感器 | 在 Hardwares 对应分组加字段，并在该分组构造方法中查找/初始化 |
| 修改配置中的硬件名字 | 修改 Hardwares 中对应字符串，与 Robot Configuration 大小写一致 |
| 重命名 Java 字段，如 servo1 | 同时修改 Hardwares 声明、初始化左侧和所有调用处；AS 的 Rename 重构可统一处理 |
| 改电机转向 | 修改 Hardwares.Motors 内对应的 setDirection |
| 改舵机开合角度、等待时间 | 修改 Auto 顶部 CLOSED_ANGLE_DEGREES、OPEN_ANGLE_DEGREES、SERVO_WAIT_SECONDS；角度填 0~360 度 |
| 改路径终点、朝向 | 修改 Auto 的 Pose；x/y 为 inch，heading 为 rad；需要新增路径时同步修改 buildPaths 和 switch |
| 改动作顺序 | 修改 Auto 的 switch(step)；每步完成后改编号，进入等待时才重置计时器 |
| 改手柄操作、整体速度 | 修改 MecanumDrive 的输入字段，或在四轮输出前统一乘 speedScale |
| 改定位器或 Pedro 调参 | 硬件型号、方向和偏置在 Hardwares；质量、速度、PID 在 Constants |

注释中的 `motor1`、`servo3`、距离传感器和 `speedScale` 都是修改示范，不是已经创建的变量。需要使用时按注释补齐声明和初始化。

## 文件职责

- `Hardwares.java`：`motors`、`servos`、`sensors` 的设备字段与初始化，包括 Pedro 底盘/定位器的创建。
- `pedroPathing/Constants.java`：仅保存质量、速度、PID 等 Pedro 调参数据。
- `ServoPedroAutoExample.java`：普通 FTC OpMode，使用一个 `switch(step)` 管理顺序。
- `MecanumDrive.java`：沿用原来的电机名称、方向和控制计算；通过 `new Hardwares(hardwareMap,false)` 只加载电机，此时 `servos/sensors` 为 null，不要求安装示例设备。

## Pedro 调用

```java
follower=hardwares.createFollower();
follower.setStartingPose(startPose);
path=follower.pathBuilder()
        .addPath(new BezierLine(startPose,endPose))
        .setLinearHeadingInterpolation(startPose.getHeading(),endPose.getHeading())
        .build();
follower.followPath(path,maxPower,false); // 启动一次
follower.update(); // 每次 loop 都调用
follower.isBusy(); // false 表示跟随结束，也可能是路径结束超时
follower.breakFollowing(); // 取消路径
```

## 占位参数

在 Hardwares 填舵机/Pinpoint 名称、Pinpoint 型号、方向与偏置；电机名称和方向目前沿用现有 MecanumDrive。Constants 填当前车的调参数据。Auto 填舵机两个 0~360 度角度、等待秒数、功率和三个 Pose（x/y 为 inch，heading 为 rad）。注意：舵机角度用度，Pedro Pose 的 heading 仍用弧度。

所有 `NaN`、`null`、`REPLACE_` 都是待填写标记。配置并完成单机构测试后开启 `PEDRO_CONFIGURED` 和 `CONFIGURATION_COMPLETE`。队伍自己要运行的 Auto **不能添加或保留 `@Disabled`**，否则 Driver Station 不会显示；仓库占位示例保留它。

示例顺序：关闭舵机 → 到动作点 → 打开舵机 → 计时等待 → 到停车点。

参考 [FTC16093 Premier](https://github.com/lucasnotfound59/FTC16093-2026DECODE-Premier/tree/888b0c7894c8badfc6a7bdb4fa558db67446eaed/TeamCode/src/main/java/org/firstinspires/ftc/teamcode) 的硬件分组和 Pedro 2.1.2 调用方式。
