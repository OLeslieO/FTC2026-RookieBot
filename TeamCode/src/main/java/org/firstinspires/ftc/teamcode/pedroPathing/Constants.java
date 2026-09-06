package org.firstinspires.ftc.teamcode.pedroPathing;

import com.pedropathing.control.FilteredPIDFCoefficients;
import com.pedropathing.control.PIDFCoefficients;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.drivetrains.MecanumConstants;

/** 这里只存 Pedro 调参数据。硬件声明、名字、方向和初始化都在 Hardwares。 */
public final class Constants {
    // 第 1 步：先验证定位器读数/方向/距离，再按 Pedro 调参流程测出下面的值。
    // Double.NaN 表示“尚未填写”，不是默认调参值。把每个 NaN 改为当前机器人的测量/调参结果。
    // TODO: 填完 Hardwares 和本文件，并完成调参后，再改成 true。仅改开关不会自动完成配置。
    public static final boolean PEDRO_CONFIGURED=false;

    // 本类只提供 static 方法，不需要 new Constants()；Hardwares 会直接调用这些方法。
    private Constants() {}

    // 第 2 步：提供运动控制参数。每次返回一份新的参数对象，不会在这里连接或移动硬件。
    // 下面 .xxx(...) 是链式配置：每行填一个参数；想修改哪一项就改对应行的数值。
    public static FollowerConstants followerConstants() {
        return new FollowerConstants()
                // 机器人运行状态下的质量，单位 kg；换机构或电池布置后重新测量。
                .mass(Double.NaN) // kg
                // 撤掉驱动功率后的前进/横向减速度，单位 inch/s²，按调参结果保留符号。
                // 这两项用于预测滑行和制动距离，不是电机方向，也不是最大加速度。
                .forwardZeroPowerAcceleration(Double.NaN) // inch/s²
                .lateralZeroPowerAcceleration(Double.NaN)
                // 转弯时的向心补偿系数；按当前车弯道测试调整，不要当作速度系数使用。
                .centripetalScaling(Double.NaN)
                // 平移纠偏：修正机器人与路径之间的位置误差。四个参数依次是 P、I、D、F。
                // P 响应当前误差，I 累计误差，D 响应误差变化，F 为前馈项；按对应调参步骤分别修改。
                .translationalPIDFCoefficients(new PIDFCoefficients(
                        Double.NaN,Double.NaN,Double.NaN,Double.NaN))
                // 朝向纠偏：修正机器人 heading，参数仍为 P、I、D、F，与平移 PID 分开调。
                .headingPIDFCoefficients(new PIDFCoefficients(
                        Double.NaN,Double.NaN,Double.NaN,Double.NaN))
                // 沿路径驱动控制：参数顺序是 P、I、D、T、F，注意这里比上面多一个滤波参数 T。
                // 修改时不要把 T 和 F 的位置写反，也不要直接复制其他机器人的系数。
                .drivePIDFCoefficients(new FilteredPIDFCoefficients(
                        Double.NaN,Double.NaN,Double.NaN,Double.NaN,Double.NaN));
    }

    // 第 3 步：提供麦轮的性能数据。电机名字/方向在 Hardwares 中统一补入这份参数。
    public static MecanumConstants driveConstants() {
        return new MecanumConstants()
                // xVelocity/yVelocity 是实测前进/横移最大速度，不是目标位置，也不是摇杆输入。
                .xVelocity(Double.NaN) // 前进/横移最大速度，inch/s
                .yVelocity(Double.NaN)
                // 要调整底盘总体功率上限，改这里；Auto 的 PATH_MAX_POWER 还会限制单次路径功率。
                .maxPower(Double.NaN); // (0,1]，实测安全功率上限
    }
}
