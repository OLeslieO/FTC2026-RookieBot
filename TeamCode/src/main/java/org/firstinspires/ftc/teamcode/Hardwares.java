package org.firstinspires.ftc.teamcode;

import com.pedropathing.follower.Follower;
import com.pedropathing.ftc.FollowerBuilder;
import com.pedropathing.ftc.drivetrains.MecanumConstants;
import com.pedropathing.ftc.localization.constants.PinpointConstants;
import com.pedropathing.paths.PathConstraints;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

/**
 * 机器人硬件清单：新增设备时先修改这里，再到 Auto/TeleOp 中使用。
 * 阅读顺序：构造方法 → Motors → Servos → Sensors → createFollower()。
 * 例如 hardwares.servos.servo1：先取舵机分组，再取这个分组内的 servo1 设备。
 */
public final class Hardwares {
    // 第 1 步：按设备类型分组。public 允许 Auto 直接访问，final 表示初始化后不能换成另一个对象。
    // final 不会锁住硬件：仍然可以调用 setPower()/setPosition() 改变电机功率或舵机目标位置。
    public final Motors motors;
    public final Servos servos;
    public final Sensors sensors;
    // SDK 提供的硬件目录，保存它是为了后面让 Pedro 找到同一组设备。
    private final HardwareMap hardwareMap;

    // 第 2 步：new Hardwares(hardwareMap) 会进入这里，默认初始化全部三个分组。
    // 想在 Auto 中增加硬件，不用增加一次 new；把设备加进下方对应分组即可。
    public Hardwares(HardwareMap hardwareMap) {
        this(hardwareMap,true);
    }

    /**
     * 第 3 步：实际建立各组设备引用。
     * includeAutoHardware=true：加载电机、舵机和传感器，全部名称都必须能在配置中找到。
     * includeAutoHardware=false：只加载电机，适合本项目的 MecanumDrive 底盘测试。
     * false 时 servos/sensors 为 null（没有创建），不能访问它们的字段或创建 Pedro。
     */
    public Hardwares(HardwareMap hardwareMap,boolean includeAutoHardware) {
        this.hardwareMap=hardwareMap;
        motors=new Motors(hardwareMap);
        // 条件 ? A : B 的意思是：条件为 true 用 A，否则用 B。
        servos=includeAutoHardware?new Servos(hardwareMap):null;
        sensors=includeAutoHardware?new Sensors(hardwareMap):null;
    }

    public static final class Motors {
        // 第 4 步：填写 Robot Configuration 中的名字，大小写必须一致。
        // 要改接线端口，在 Robot Configuration 中改端口；Java 根据名字查找，不写端口号。
        // 这里沿用现有 MecanumDrive 的名字。重命名设备时只改对应字符串。
        private static final String LEFT_FRONT_NAME="leftFront";
        private static final String LEFT_REAR_NAME="leftRear";
        private static final String RIGHT_FRONT_NAME="rightFront";
        private static final String RIGHT_REAR_NAME="rightRear";
        // 第 5 步：声明可供业务代码使用的设备字段。DcMotorEx 是可控制功率/速度的电机类型。
        // 新增 motor1：在这里加 public final DcMotorEx motor1;
        // 再在下方构造方法中加 motor1=hardwareMap.get(DcMotorEx.class,"motor1");
        // 然后就能用 hardwares.motors.motor1.setPower(power)。声明和初始化要一起添加。
        public final DcMotorEx leftFront,leftRear,rightFront,rightRear;

        private Motors(HardwareMap hardwareMap) {
            // 第 6 步：get(设备类型,配置名字) 找到真实设备，并把引用赋给刚声明的字段。
            // 找不到会报初始化错误；先核对配置中的类型、名称和当前启用的配置文件。
            leftFront=hardwareMap.get(DcMotorEx.class,LEFT_FRONT_NAME);
            leftRear=hardwareMap.get(DcMotorEx.class,LEFT_REAR_NAME);
            rightFront=hardwareMap.get(DcMotorEx.class,RIGHT_FRONT_NAME);
            rightRear=hardwareMap.get(DcMotorEx.class,RIGHT_REAR_NAME);
            // 第 7 步：统一设置电机方向。FORWARD/REVERSE 定义正功率对应的转向。
            // 保留原 MecanumDrive 配置；换车后若某轮正转方向不对，在这里修改对应方向。
            // 不要在 Auto 和 TeleOp 中分别设置方向，否则同一个字段会有两套含义。
            leftFront.setDirection(DcMotorSimple.Direction.FORWARD);
            leftRear.setDirection(DcMotorSimple.Direction.REVERSE);
            rightFront.setDirection(DcMotorSimple.Direction.REVERSE);
            rightRear.setDirection(DcMotorSimple.Direction.REVERSE);
        }
    }

    public static final class Servos {
        // 第 8 步：声明舵机字段。servo1/servo2 是 Java 字段名，可自己起名。
        // Java 字段名和下方配置字符串不必相同：前者供代码使用，后者用于查找硬件。
        public final Servo servo1, servo2;

        private Servos(HardwareMap hardwareMap) {
            // 第 9 步：查找舵机，这里不调用 setPosition，因此不会主动发送初始位置指令。
            // TODO: 第一个占位字符串改成实物配置名称；第二个设备当前要求配置名为 "Servo2"。
            // 如果没有第二个舵机，要同时删掉上方 servo2 声明和下方 servo2 初始化。
            // 新增 servo3 时反过来：加字段、加 get，再在 Auto 用 hardwares.servos.servo3。
            servo1=hardwareMap.get(Servo.class,"REPLACE_WITH_SERVO_NAME");
            servo2=hardwareMap.get(Servo.class, "Servo2");
        }
    }

    public static final class Sensors {
        // 第 10 步：传感器也按“配置名称 → 字段 → 查找”的顺序添加。
        // 本例是 Pinpoint 定位模块；将名字改为 Robot Configuration 中的实际名称。
        private static final String PINPOINT_NAME="REPLACE_PINPOINT";
        public final GoBildaPinpointDriver pinpoint;

        private Sensors(HardwareMap hardwareMap) {
            pinpoint=hardwareMap.get(GoBildaPinpointDriver.class,PINPOINT_NAME);
            // Pinpoint 的配置由下面 createFollower() 中的 Pedro 初始化统一完成。
            // 例如新增距离传感器：导入 DistanceSensor，在本类声明 public final DistanceSensor distance;
            // 然后在这里写 distance=hardwareMap.get(DistanceSensor.class,"distance");
            // 读取时在业务代码调用 hardwares.sensors.distance.getDistance(DistanceUnit.CM)。
            // 不同传感器需要不同的初始化参数，要跟随对应设备的 SDK 示例，不能套用 Pinpoint 配置。
        }
    }

    /**
     * 第 11 步：将硬件接入 Pedro。Auto 在 init() 中调用一次 hardwares.createFollower()。
     * Pedro 内部会设置电机模式并配置定位器，所以这个入口也集中在 Hardwares。
     * 不要在 loop() 中重复创建 Follower，否则会重复初始化并丢掉正在执行的路径状态。
     */
    public Follower createFollower() {
        // 配置锁：填完本文件的硬件参数和 Constants 的调参数据，再开启 PEDRO_CONFIGURED。
        if(!Constants.PEDRO_CONFIGURED) {
            throw new IllegalStateException("请先填写 Hardwares 和 Constants 中的 Pedro 参数");
        }
        if(sensors==null) throw new IllegalStateException("创建 Pedro 需要完整 Hardwares");

        // 第 12 步：把 Constants 的速度/功率参数与这里的电机名字、方向组合起来。
        // getDirection() 读取上面已设置的方向，让 Pedro 和手动驾驶使用同一套硬件定义。
        MecanumConstants driveConstants=Constants.driveConstants()
                .leftFrontMotorName(Motors.LEFT_FRONT_NAME)
                .leftRearMotorName(Motors.LEFT_REAR_NAME)
                .rightFrontMotorName(Motors.RIGHT_FRONT_NAME)
                .rightRearMotorName(Motors.RIGHT_REAR_NAME)
                .leftFrontMotorDirection(motors.leftFront.getDirection())
                .leftRearMotorDirection(motors.leftRear.getDirection())
                .rightFrontMotorDirection(motors.rightFront.getDirection())
                .rightRearMotorDirection(motors.rightRear.getDirection());

        // 第 13 步：告诉 Pinpoint 两个里程轮装在哪里、用哪种轮、计数朝哪个方向增加。
        // Double.NaN 和 null 都是待填写标记；要用实测值/实际型号替换，不能直接运行。
        // 此处选择 INCH，两个偏置也必须用 inch；改成 CM 时要一起换算两个偏置值。
        // 编码器方向要用手推机器人验证；它与电机的 FORWARD/REVERSE 不是同一套枚举。
        PinpointConstants localizerConstants=new PinpointConstants()
                .hardwareMapName(Sensors.PINPOINT_NAME)
                .distanceUnit(DistanceUnit.INCH)
                .forwardPodY(Double.NaN) // TODO: 前向轮的横向偏置，inch
                .strafePodX(Double.NaN) // TODO: 横向轮的前向偏置，inch
                .encoderResolution(null) // TODO: GoBildaPinpointDriver.GoBildaOdometryPods 的实际型号
                .forwardEncoderDirection(null) // TODO: EncoderDirection.FORWARD 或 REVERSED
                .strafeEncoderDirection(null);

        // 第 14 步：按顺序组装 Follower。点号链式调用是在设置同一个 builder，最后 build() 完成创建。
        // SDK 根据名字返回上面同一组设备；不要在 Auto 中再初始化或重置定位器。
        // pathConstraints：复制 Pedro 默认的路径结束阈值，需要调结束精度/超时才修改这部分。
        // mecanumDrivetrain：接入四轮麦克纳姆底盘；pinpointLocalizer：接入 Pinpoint 定位器。
        // 换定位硬件时要连同 Sensors 声明、查找、配置和此处的 localizer 调用一起改。
        return new FollowerBuilder(Constants.followerConstants(),hardwareMap)
                .pathConstraints(PathConstraints.defaultConstraints.copy())
                .mecanumDrivetrain(driveConstants)
                .pinpointLocalizer(localizerConstants)
                .build();
    }
}
