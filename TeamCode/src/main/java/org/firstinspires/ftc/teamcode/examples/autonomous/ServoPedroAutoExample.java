package org.firstinspires.ftc.teamcode.examples.autonomous;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.teamcode.Hardwares;

/** Hardwares 初始化所有硬件；Auto 直接使用分组字段，管理动作顺序。 */
// 第 1 步：注册自动程序。name 是 Driver Station 显示的名称，复制成自己的 Auto 时要改名。
// group 只是菜单分组；修改 Java 类名时，文件名也必须一起修改。
@Autonomous(name="Servo Pedro Auto Example",group="Examples")
// 只有示例文件使用 @Disabled。编写队伍自己的 Auto 时不能添加或保留 @Disabled，
// 否则该 OpMode 不会出现在 Driver Station 中。
@Disabled
public final class ServoPedroAutoExample extends OpMode {
    // 第 2 步：集中填写本次 Auto 的动作参数；硬件名字、声明和初始化只在 Hardwares 中改。
    // Double.NaN 表示“尚未填写”，不是合法的运动参数。static final 表示整段程序共用的固定值。
    // TODO: 填完本文件、Hardwares 和 Constants，并完成单机构测试，再把下面的锁改成 true。
    // Constants.PEDRO_CONFIGURED 是另一道配置锁，也需要在 Pedro 参数验证完成后开启。
    private static final boolean CONFIGURATION_COMPLETE=false;
    // 要改变开合程度，就修改这两个位置。0~1 对应舵机行程比例，不保证是 0~180 度。
    private static final double CLOSED_POSITION=Double.NaN; // 舵机位置 0~1，不是角度
    private static final double OPEN_POSITION=Double.NaN;
    // setPosition 只发送目标，不会等待转动完成；这里填实测需要等待的秒数（大于 0）。
    private static final double SERVO_WAIT_SECONDS=Double.NaN;
    private static final double PATH_MAX_POWER=Double.NaN; // (0,1]，从低功率开始验证

    // 第 3 步：定义“从哪里出发、在哪里做动作、最后停哪里”。改路线先修改这三个点。
    // Pose(x,y,heading)：Pedro 场地坐标 x/y 单位 inch，heading 单位 rad。
    // 例如 Math.toRadians(90) 表示 90 度；不要把数字 90 直接当成弧度填进去。
    // START_POSE 必须匹配摆车位置，各段起终点的 x/y 应不同；不要照抄别人的场地坐标。
    private static final Pose START_POSE=new Pose(Double.NaN,Double.NaN,Double.NaN);
    private static final Pose ACTION_POSE=new Pose(Double.NaN,Double.NaN,Double.NaN);
    private static final Pose PARK_POSE=new Pose(Double.NaN,Double.NaN,Double.NaN);

    // 第 4 步：保存程序运行期间要反复使用的对象。这些是引用，真正创建在 init() 中。
    // Follower 负责更新定位并计算电机输出；PathChain 保存提前建好的路径。
    private Hardwares hardwares;
    private Follower follower;
    private PathChain toAction;
    private PathChain toPark;
    // 计时器用于“等一段时间但不堵住 loop”；reset() 清零，seconds() 返回清零后经过的秒数。
    private final ElapsedTime timer=new ElapsedTime();
    private int step=-1; // -1=未启动/停止，0=去动作点，1=等舵机，2=去停车点，3=完成
    private boolean ready=false; // 只有初始化全部成功才允许 start()/loop() 执行动作

    // 第 5 步：Driver Station 按 INIT 时调用一次；这里只准备对象和路径，不主动走路线。
    @Override
    public void init() {
        // 未配置时不查找硬件，不发送电机或舵机指令。
        if(!CONFIGURATION_COMPLETE) {
            telemetry.addLine("占位模板：请先填写 Auto、Hardwares 和 Constants 中的 TODO 参数。");
            telemetry.update();
            return;
        }
        try {
            // 5.1 创建硬件清单；若增加/删除设备，去 Hardwares 改，不要在这里另查一次设备。
            hardwares=new Hardwares(hardwareMap);
            // 5.2 让 Pedro 接管同一组底盘电机和 Pinpoint。只创建一次，后面一直复用。
            follower=hardwares.createFollower();
            // 5.3 给定位器设定起点。这是告诉程序“当前在哪”，不会把实物移动到这里。
            follower.setStartingPose(START_POSE); // 必须对应实物起始位置
            // 5.4 建路径只是存下几何信息；followPath() 才会启动跟随。
            buildPaths();
            ready=true;
        } catch(RuntimeException error) {
            // 名字不匹配等初始化错误会走到这里；先停止，再把原因显示到 Driver Station。
            stop();
            telemetry.addData("初始化失败",error.getMessage());
            telemetry.update();
        }
    }

    // 第 6 步：建立两段直线路径。修改某段路线时改 BezierLine 的起终点。
    // 想增加第三段：先加 PathChain 字段，在这里构建，再在下面 switch 中增加对应步骤。
    private void buildPaths() {
        // pathBuilder() 开始描述路径，addPath() 添加线段，build() 得到可交给 Pedro 的路径对象。
        toAction=follower.pathBuilder()
                .addPath(new BezierLine(START_POSE,ACTION_POSE))
                // 机器人沿路径行驶时，朝向从起点 heading 逐步过渡到终点 heading。
                .setLinearHeadingInterpolation(START_POSE.getHeading(),ACTION_POSE.getHeading())
                .build();
        // 第二段从动作点接到停车点；相邻路径端点要接上，不能让程序假定机器人瞬移。
        toPark=follower.pathBuilder()
                .addPath(new BezierLine(ACTION_POSE,PARK_POSE))
                .setLinearHeadingInterpolation(ACTION_POSE.getHeading(),PARK_POSE.getHeading())
                .build();
        // 固定朝向时可改用 .setConstantHeadingInterpolation(heading)。
    }

    // 第 7 步：Driver Station 按 START 时调用一次，设置机构并启动第一段路径。
    @Override
    public void start() {
        if(!ready) return;
        try {
            // 7.1 操作 Hardwares 中已声明的舵机。要换成第二个舵机，就把 servo1 改成 servo2。
            // 这里发送关闭目标后就继续走；如果必须等关闭完成再行驶，需要额外加一个计时步骤。
            hardwares.servos.servo1.setPosition(CLOSED_POSITION);
            // 7.2 参数依次是路径、功率上限、结束后是否保持终点。false 表示结束后不保持。
            // 此调用不等待行驶结束；后续通过 loop 中的 update()/isBusy() 推进和检查。
            follower.followPath(toAction,PATH_MAX_POWER,false);
            // 7.3 告诉 loop：接下来检查“去动作点”这一步。
            step=0;
        } catch(RuntimeException error) {
            stop();
            telemetry.addData("启动失败",error.getMessage());
            telemetry.update();
        }
    }

    // 第 8 步：START 后 SDK 会反复调用 loop()。每次只做少量工作，检查是否可以进入下一步。
    @Override
    public void loop() {
        if(!ready) return;
        try {
            // 8.1 读取定位、更新路径控制和电机输出。等待舵机时也必须继续调用。
            follower.update(); // 每次 loop 都要调用，不能用 sleep 阻塞寻路
            // 8.2 switch 只执行当前 step 对应的分支；break 退出 switch，不是结束整个 Auto。
            // 想改动作顺序，就在这些分支内调整动作和 step 的下一个编号。
            switch(step) {
                case 0:
                    // 正在去动作点：仍忙就留在 step 0，下次 loop 再检查。
                    // isBusy=false 表示 Pedro 已结束跟随（也可能触发路径结束超时），不保证实物零误差。
                    if(follower.isBusy()) break;
                    // 跟随结束后打开舵机；要两只一起动，可在这里再加 servo2.setPosition(...)。
                    hardwares.servos.servo1.setPosition(OPEN_POSITION);
                    // 只在进入等待阶段时清零一次；若每次 loop 都清零，就永远等不到设定时间。
                    timer.reset();
                    step=1;
                    break;
                case 1:
                    // 正在等舵机：未到时间则保持 step 1，下一次 loop 仍会执行 follower.update()。
                    // 要改等待时长，只改顶部 SERVO_WAIT_SECONDS，不需要改这里的判断。
                    if(timer.seconds()<SERVO_WAIT_SECONDS) break;
                    // 等待结束，启动第二段路径并切到 step 2，防止每帧重新发起同一路径。
                    follower.followPath(toPark,PATH_MAX_POWER,false);
                    step=2;
                    break;
                case 2:
                    // 正在去停车点：结束后取消跟随，step 3 表示所有计划动作都已完成。
                    if(follower.isBusy()) break;
                    follower.breakFollowing();
                    step=3;
                    break;
                default:
                    // step 3 不再发起新动作。想加下一步，可增加 case 3，并在完成时改为 step 4。
                    break;
            }
            // 8.3 把状态发给 Driver Station；卡住时先看 step、busy 和 pose，定位是哪一步的问题。
            // 想观察更多数据，在这里增加 addData("标签",值)，最后 update() 才会提交显示。
            telemetry.addData("step",step);
            telemetry.addData("busy",follower.isBusy());
            telemetry.addData("pose",follower.getPose());
        } catch(RuntimeException error) {
            stop(); // 出错后直接停，不再读取可能故障的 follower
            telemetry.addData("运行失败",error.getMessage());
        }
        telemetry.update();
    }

    // 第 9 步：按 STOP 或出错时停下。ready=false 禁止后续动作，breakFollowing() 取消 Pedro 控制。
    // 这里不自动改变舵机位置；需要停止时回位，应先确认机构安全再加入对应 setPosition()。
    @Override
    public void stop() {
        ready=false;
        step=-1;
        if(follower==null) return; // 还没创建成功就被停止时，无需调用 Pedro
        try {
            follower.breakFollowing();
        } catch(RuntimeException error) {
            telemetry.addData("停止失败",error.getMessage());
        }
    }
}
