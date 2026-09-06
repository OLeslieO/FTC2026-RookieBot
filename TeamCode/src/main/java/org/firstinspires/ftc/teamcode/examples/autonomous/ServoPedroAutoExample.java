package org.firstinspires.ftc.teamcode.examples.autonomous;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import org.firstinspires.ftc.teamcode.examples.hardware.ExampleServoHardware;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

/** Demonstrates structure and API calls only. This is not robot-ready autonomous code. */
@Autonomous(name="Servo Pedro Auto Example",group="Examples")
// 只有示例文件使用 @Disabled。编写队伍自己的 Auto 时不能添加或保留 @Disabled，
// 否则该 OpMode 不会出现在 Driver Station 中。
@Disabled
public final class ServoPedroAutoExample extends OpMode {
    // CONFIGURE HERE START: all values are intentionally unusable sentinels.
    private static final boolean CONFIGURATION_COMPLETE=false;
    private static final String SERVO_NAME="REPLACE_WITH_SERVO_NAME";
    private static final double SERVO_CLOSED_POSITION=Double.NaN;
    private static final double SERVO_OPEN_POSITION=Double.NaN;
    private static final Pose START_POSE=new Pose(Double.NaN,Double.NaN,Double.NaN);
    private static final Pose ACTION_POSE=new Pose(Double.NaN,Double.NaN,Double.NaN);
    private static final Pose PARK_POSE=new Pose(Double.NaN,Double.NaN,Double.NaN);
    private static final double SERVO_WAIT_SECONDS=Double.NaN;
    private static final double PATH_MAX_POWER=Double.NaN;
    // CONFIGURE HERE END

    private enum AutoState {
        READY,DRIVE_TO_ACTION,WAIT_FOR_SERVO,DRIVE_TO_PARK,DONE,SAFETY_STOP,STOPPED
    }

    private enum LastServoCommand {
        NONE,CLOSED,OPEN
    }

    static final class FollowerTelemetrySnapshot {
        private final boolean available;
        private final boolean busy;
        private final Pose pose;
        private final String failure;

        private FollowerTelemetrySnapshot(
                boolean available,boolean busy,Pose pose,String failure) {
            this.available=available;
            this.busy=busy;
            this.pose=pose;
            this.failure=failure;
        }

        static FollowerTelemetrySnapshot available(boolean busy,Pose pose) {
            return new FollowerTelemetrySnapshot(true,busy,pose,"none");
        }

        static FollowerTelemetrySnapshot unavailable(RuntimeException exception) {
            return new FollowerTelemetrySnapshot(false,false,null,describe(exception));
        }

        boolean isAvailable() {
            return available;
        }

        boolean isBusy() {
            return busy;
        }

        Pose getPose() {
            return pose;
        }

        String getFailure() {
            return failure;
        }
    }

    private final List<String> configurationIssues=new ArrayList<>();
    private final ElapsedTime stateTimer=new ElapsedTime();
    private AutoState autoState=AutoState.SAFETY_STOP;
    private LastServoCommand lastServoCommand=LastServoCommand.NONE;
    private String runtimeFailure="none";
    private boolean safetyLocked=true;
    private ExampleServoHardware robot;
    private Follower follower;
    private PathChain actionPath;
    private PathChain parkPath;

    @Override
    public void init() {
        validateConfiguration();
        if(configurationIssues.isEmpty()) initializeConfiguredResources();
        safetyLocked=!configurationIssues.isEmpty()||robot==null||follower==null;
        transitionTo(safetyLocked?AutoState.SAFETY_STOP:AutoState.READY);
        emitTelemetry();
    }

    @Override
    public void init_loop() {
        emitTelemetry();
    }

    @Override
    public void start() {
        if(safetyLocked) return;
        try {
            // Auto code calls a mechanism-level method, not Servo.setPosition() directly.
            robot.close();
            lastServoCommand=LastServoCommand.CLOSED;
            transitionTo(AutoState.DRIVE_TO_ACTION);
            commandPath(actionPath);
        } catch(RuntimeException exception) {
            enterSafetyStop(exception);
        }
    }

    @Override
    public void loop() {
        if(safetyLocked) {
            emitTelemetry();
            return;
        }
        try {
            // Pedro is non-blocking: every iterative OpMode loop must update the Follower.
            follower.update();
            switch(autoState) {
                case DRIVE_TO_ACTION:
                    // isBusy() becomes false after Pedro finishes the active path.
                    if(!follower.isBusy()) {
                        robot.open();
                        lastServoCommand=LastServoCommand.OPEN;
                        transitionTo(AutoState.WAIT_FOR_SERVO);
                    }
                    break;
                case WAIT_FOR_SERVO:
                    // ElapsedTime keeps waiting non-blocking; loop() must not block.
                    if(stateTimer.seconds()>=SERVO_WAIT_SECONDS) {
                        transitionTo(AutoState.DRIVE_TO_PARK);
                        commandPath(parkPath);
                    }
                    break;
                case DRIVE_TO_PARK:
                    if(!follower.isBusy()) transitionTo(AutoState.DONE);
                    break;
                case READY:
                case DONE:
                    break;
                default:
                    throw new IllegalStateException("unexpected Auto state "+autoState);
            }
        } catch(RuntimeException exception) {
            enterSafetyStop(exception);
        }
        emitTelemetry();
    }

    @Override
    public void stop() {
        safetyLocked=true;
        transitionTo(AutoState.STOPPED);
        breakFollowingBestEffort();
    }

    private void validateConfiguration() {
        configurationIssues.clear();
        if(!CONFIGURATION_COMPLETE) configurationIssues.add("CONFIGURATION_COMPLETE is false");
        try {
            ExampleServoHardware.validateConfiguration(
                    SERVO_NAME,SERVO_CLOSED_POSITION,SERVO_OPEN_POSITION);
        } catch(IllegalArgumentException exception) {
            configurationIssues.add(exception.getMessage());
        }
        validatePose("START_POSE",START_POSE);
        validatePose("ACTION_POSE",ACTION_POSE);
        validatePose("PARK_POSE",PARK_POSE);
        if(!Double.isFinite(SERVO_WAIT_SECONDS)||SERVO_WAIT_SECONDS<=0.0) {
            configurationIssues.add("replace SERVO_WAIT_SECONDS with a measured positive value");
        }
        if(!Double.isFinite(PATH_MAX_POWER)||PATH_MAX_POWER<=0.0||PATH_MAX_POWER>1.0) {
            configurationIssues.add("replace PATH_MAX_POWER with a reviewed value in (0,1]");
        }
        if(samePosition(START_POSE,ACTION_POSE)||samePosition(ACTION_POSE,PARK_POSE)) {
            configurationIssues.add("route poses must use different x/y positions");
        }
    }

    private void initializeConfiguredResources() {
        try {
            robot=new ExampleServoHardware(
                    hardwareMap,SERVO_NAME,SERVO_CLOSED_POSITION,SERVO_OPEN_POSITION);

            // Keep drivetrain/localizer construction centralized behind this factory.
            follower=Constants.createFollower(hardwareMap);

            // setStartingPose() must match the robot's measured physical starting pose.
            follower.setStartingPose(START_POSE);
            buildPaths();
        } catch(RuntimeException exception) {
            runtimeFailure=describe(exception);
            configurationIssues.add("resource initialization failed: "+runtimeFailure);
        }
    }

    private void buildPaths() {
        // pathBuilder() creates a reusable PathChain before start() begins.
        actionPath=follower.pathBuilder()
                .addPath(new BezierLine(START_POSE,ACTION_POSE))
                .setLinearHeadingInterpolation(START_POSE.getHeading(),ACTION_POSE.getHeading())
                .build();
        parkPath=follower.pathBuilder()
                .addPath(new BezierLine(ACTION_POSE,PARK_POSE))
                .setLinearHeadingInterpolation(ACTION_POSE.getHeading(),PARK_POSE.getHeading())
                .build();
    }

    private void commandPath(PathChain path) {
        if(safetyLocked||follower==null||path==null) {
            throw new IllegalStateException("path command rejected by safety guard");
        }
        // followPath() starts movement and returns immediately; loop() continues updating it.
        follower.followPath(path,PATH_MAX_POWER,false);
    }

    private void validatePose(String name,Pose pose) {
        if(pose==null||!Double.isFinite(pose.getX())||!Double.isFinite(pose.getY())
                ||!Double.isFinite(pose.getHeading())) {
            configurationIssues.add("replace "+name+" with a measured Pedro pose");
        }
    }

    static boolean samePosition(Pose first,Pose second) {
        return first!=null&&second!=null
                &&first.getX()==second.getX()
                &&first.getY()==second.getY();
    }

    private void transitionTo(AutoState next) {
        if(autoState==next) return;
        autoState=next;
        stateTimer.reset();
    }

    private void enterSafetyStop(RuntimeException exception) {
        runtimeFailure=describe(exception);
        safetyLocked=true;
        transitionTo(AutoState.SAFETY_STOP);
        breakFollowingBestEffort();
    }

    private static String describe(RuntimeException exception) {
        return exception.getClass().getSimpleName()+": "+exception.getMessage();
    }

    static FollowerTelemetrySnapshot captureFollowerTelemetry(
            BooleanSupplier busySupplier,Supplier<Pose> poseSupplier) {
        try {
            boolean busy=busySupplier.getAsBoolean();
            Pose pose=poseSupplier.get();
            if(pose==null) throw new IllegalStateException("Follower returned no pose");
            return FollowerTelemetrySnapshot.available(busy,pose);
        } catch(RuntimeException exception) {
            return FollowerTelemetrySnapshot.unavailable(exception);
        }
    }

    private void breakFollowingBestEffort() {
        if(follower==null) return;
        try {
            // STOP should cancel the active Pedro path without hiding the logical safety lock.
            follower.breakFollowing();
        } catch(RuntimeException ignored) {
            // Logical stop is already active; cancellation is best effort.
        }
    }

    private void emitTelemetry() {
        telemetry.addData("configuration complete",CONFIGURATION_COMPLETE);
        telemetry.addData("safety locked",safetyLocked);
        telemetry.addData("auto state",autoState);
        telemetry.addData("runtime failure",runtimeFailure);
        for(String issue:configurationIssues) telemetry.addLine("CONFIG: "+issue);
        if(follower!=null) {
            FollowerTelemetrySnapshot snapshot=
                    captureFollowerTelemetry(follower::isBusy,follower::getPose);
            if(snapshot.isAvailable()) {
                telemetry.addData("follower busy",snapshot.isBusy());
                telemetry.addData("x (in)",snapshot.getPose().getX());
                telemetry.addData("y (in)",snapshot.getPose().getY());
                telemetry.addData("heading (rad)",snapshot.getPose().getHeading());
            } else {
                telemetry.addData("follower telemetry","unavailable: "+snapshot.getFailure());
            }
        }
        telemetry.addData("last servo command",lastServoCommand);
        telemetry.addData("state elapsed (s)",stateTimer.seconds());
        telemetry.update();
    }
}
