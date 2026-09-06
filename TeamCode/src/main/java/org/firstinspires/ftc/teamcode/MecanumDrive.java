package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

// 第 1 步：注册手动驾驶程序。没有写 name 时，菜单名称默认使用类名 MecanumDrive。
// 想自定义显示名，可写 @TeleOp(name="你的名称",group="example")。
@TeleOp(group = "example")
public class MecanumDrive extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        // 第 2 步：初始化底盘。名字、设备字段和电机方向全部去 Hardwares.Motors 中修改。
        // false 只加载电机；如果以后要在本 TeleOp 使用舵机/传感器，改用 new Hardwares(hardwareMap)。
        // 完整初始化会要求所有已声明设备都存在；新增设备先在 Hardwares 对应分组声明并初始化。
        Hardwares hardwares=new Hardwares(hardwareMap,false);

        // 第 3 步：等待 Driver Station 按 START。LinearOpMode 由下面的 while 循环持续运行，
        // 与 ServoPedroAutoExample 由 SDK 反复调用 loop() 的写法不同，不要把两种生命周期混用。
        waitForStart();

        // 如果等待期间已经按了 STOP，直接退出，避免开始发送电机指令。
        if (isStopRequested()) return;

        // 第 4 步：START 后不断读取摇杆并更新功率，按 STOP 后条件变为 false，循环结束。
        while (opModeIsActive()) {
            // 4.1 左摇杆负责前后/横移，右摇杆左右负责转向；gamepad1 是第一只手柄。
            // 左摇杆向前推时原始 Y 为负，所以取负号，让“向前”对应正 y。
            double y = -gamepad1.left_stick_y;
            // 1.1 沿用原例的横移补偿系数；感觉横移比例不合适时再按实车调整。
            double x = gamepad1.left_stick_x * 1.1;
            // 要换转向摇杆，改这里的输入字段；不要通过改电机方向来修改手柄布局。
            double rx = gamepad1.right_stick_x;

            // 4.2 归一化：组合前进、横移、旋转后，四轮功率可能超出 [-1,1]。
            // 用同一个 denominator 缩放所有轮子，保留轮间比例，避免分别截断导致方向变形。
            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
            // 4.3 麦轮混控：每个轮子根据安装位置组合 y、x、rx，得到最终的正/负功率。
            // 若车运动方向不对，先检查轮子安装、接线对应和 Hardwares 中的方向，不要随意改公式。
            double frontLeftPower = (y + x + rx) / denominator;
            double backLeftPower = (y - x + rx) / denominator;
            double frontRightPower = (y - x - rx) / denominator;
            double backRightPower = (y + x - rx) / denominator;

            // 4.4 直接通过 Hardwares 中的电机字段发送功率，不需要在这里重新查找设备。
            // 想整体慢速驾驶，可以将下面四个功率都乘同一个 0~1 的 speedScale。
            // 新增机构的按钮控制也放在循环内，例如通过 hardwares.servos.servo1 使用已初始化的舵机。
            hardwares.motors.leftFront.setPower(frontLeftPower);
            hardwares.motors.leftRear.setPower(backLeftPower);
            hardwares.motors.rightFront.setPower(frontRightPower);
            hardwares.motors.rightRear.setPower(backRightPower);
        }
    }
}
