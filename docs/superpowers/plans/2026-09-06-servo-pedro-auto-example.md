# Servo + Pedro Pathing Auto Example Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a compile-verified, disabled-by-default teaching template that demonstrates Pedro Pathing movement, Servo sequencing, and HardwareMap encapsulation without supplying robot-ready values.

**Architecture:** `Constants` owns Pedro Follower construction, `ExampleServoHardware` owns Servo lookup and semantic commands, and `ServoPedroAutoExample` owns the non-blocking autonomous state machine. Sentinel configuration plus `@Disabled` prevents the template from being mistaken for deployable robot code.

**Tech Stack:** Java 8 target, FIRST FTC SDK 11.2.1, Pedro Pathing FTC 2.1.2, Android Gradle Plugin 8.13.2, JUnit 4.13.2.

## Global Constraints

- Keep FIRST FTC SDK at 11.2.1 and add only `com.pedropathing:ftc:2.1.2`.
- Do not introduce FTCLib Command, Road Runner, Dashboard, Panels, or FTC16093 robot-specific values.
- Do not provide real Servo names/positions, poses, motor directions, localizer offsets, PID values, power, or wait durations.
- Keep the example `@Disabled` and `CONFIGURATION_COMPLETE=false` by default.
- State in the source comment that team-written Auto code must not add or retain `@Disabled`, or it will not appear on Driver Station.
- Treat Gradle compilation as software verification only; do not claim hardware validation or deploy to Control Hub.

---

## File Map

- Modify `build.dependencies.gradle`: add Pedro's Maven repository and Pedro FTC 2.1.2 dependency.
- Modify `TeamCode/build.gradle`: add JUnit only for local validation tests.
- Create `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/pedroPathing/Constants.java`: central Follower factory and warning boundary.
- Create `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/examples/hardware/ExampleServoHardware.java`: Servo lookup, validation, and semantic commands.
- Create `TeamCode/src/test/java/org/firstinspires/ftc/teamcode/examples/hardware/ExampleServoHardwareTest.java`: pure configuration validation tests.
- Create `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/examples/autonomous/ServoPedroAutoExample.java`: disabled, sentinel-locked Auto state machine.

---

### Task 1: Add the Pedro 2.1.2 compile boundary

**Files:**
- Modify: `build.dependencies.gradle`
- Create: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/pedroPathing/Constants.java`

**Interfaces:**
- Consumes: FTC `HardwareMap`; Pedro `FollowerConstants` and `FollowerBuilder`.
- Produces: `public static Follower Constants.createFollower(HardwareMap hardwareMap)`.

- [ ] **Step 1: Create the Follower factory before adding the dependency**

```java
package org.firstinspires.ftc.teamcode.pedroPathing;

import com.pedropathing.follower.Follower;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.FollowerBuilder;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Teaching-only Pedro construction boundary.
 *
 * <p>This default FollowerConstants instance exists so the example demonstrates the API and
 * compiles. It is not a tuned drivetrain/localizer configuration. Before any real drive test,
 * replace this factory with the current robot's reviewed Pedro Quickstart constants and complete
 * localization tuning. Never copy another robot's directions, offsets, mass, PID, or constraints.
 */
public final class Constants {
    private Constants() {}

    public static Follower createFollower(HardwareMap hardwareMap) {
        return new FollowerBuilder(new FollowerConstants(),hardwareMap).build();
    }
}
```

- [ ] **Step 2: Run compilation and verify the dependency contract fails**

Run:

```bash
./gradlew :TeamCode:compileDebugJavaWithJavac
```

Expected: FAIL with missing `com.pedropathing` packages because the source contract exists but the dependency has not been installed.

- [ ] **Step 3: Add the minimal Pedro repository and dependency**

Add to `repositories {}` in `build.dependencies.gradle`:

```groovy
maven { url="https://mymaven.bylazar.com/releases" }
```

Add to `dependencies {}` in the same file:

```groovy
implementation 'com.pedropathing:ftc:2.1.2'
```

- [ ] **Step 4: Compile the factory**

Run:

```bash
./gradlew :TeamCode:compileDebugJavaWithJavac
```

Expected: `BUILD SUCCESSFUL`. Java 8 target deprecation warnings under JDK 21 are allowed.

- [ ] **Step 5: Commit Task 1**

```bash
git add build.dependencies.gradle TeamCode/src/main/java/org/firstinspires/ftc/teamcode/pedroPathing/Constants.java
git commit -m "build: add Pedro Pathing example dependency"
```

---

### Task 2: Add the Servo hardware wrapper with validation tests

**Files:**
- Modify: `TeamCode/build.gradle`
- Create: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/examples/hardware/ExampleServoHardware.java`
- Create: `TeamCode/src/test/java/org/firstinspires/ftc/teamcode/examples/hardware/ExampleServoHardwareTest.java`

**Interfaces:**
- Consumes: FTC `HardwareMap`, Servo configuration name, closed position, and open position.
- Produces: constructor `ExampleServoHardware(HardwareMap,String,double,double)`, `close()`, `open()`, and static `validateConfiguration(String,double,double)`.

- [ ] **Step 1: Add JUnit and write failing validation tests**

Add to `dependencies {}` in `TeamCode/build.gradle`:

```groovy
testImplementation 'junit:junit:4.13.2'
```

Create the test:

```java
package org.firstinspires.ftc.teamcode.examples.hardware;

import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class ExampleServoHardwareTest {
    @Test
    public void acceptsDistinctPositionsInsideServoRange() {
        ExampleServoHardware.validateConfiguration("releaseServo",0.25,0.75);
    }

    @Test
    public void rejectsPlaceholderName() {
        assertThrows(IllegalArgumentException.class,()->
                ExampleServoHardware.validateConfiguration("REPLACE_WITH_SERVO_NAME",0.25,0.75));
    }

    @Test
    public void rejectsNonFiniteOrOutOfRangePositions() {
        assertThrows(IllegalArgumentException.class,()->
                ExampleServoHardware.validateConfiguration("releaseServo",Double.NaN,0.75));
        assertThrows(IllegalArgumentException.class,()->
                ExampleServoHardware.validateConfiguration("releaseServo",0.25,1.01));
    }

    @Test
    public void rejectsIdenticalPositions() {
        assertThrows(IllegalArgumentException.class,()->
                ExampleServoHardware.validateConfiguration("releaseServo",0.5,0.5));
    }
}
```

- [ ] **Step 2: Run the focused test and verify it fails**

Run:

```bash
./gradlew :TeamCode:testDebugUnitTest --tests '*ExampleServoHardwareTest'
```

Expected: FAIL because `ExampleServoHardware` does not exist.

- [ ] **Step 3: Implement the hardware wrapper**

```java
package org.firstinspires.ftc.teamcode.examples.hardware;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Minimal example of keeping hardware lookup and raw actuator commands out of an OpMode.
 * The positions are constructor inputs because every mechanism must be measured on its own robot.
 */
public final class ExampleServoHardware {
    private final Servo releaseServo;
    private final double closedPosition;
    private final double openPosition;

    public ExampleServoHardware(
            HardwareMap hardwareMap,String servoName,double closedPosition,double openPosition) {
        if(hardwareMap==null) throw new IllegalArgumentException("hardwareMap is required");
        validateConfiguration(servoName,closedPosition,openPosition);

        // hardwareMap.get() uses the exact name from the Robot Controller configuration.
        releaseServo=hardwareMap.get(Servo.class,servoName);
        this.closedPosition=closedPosition;
        this.openPosition=openPosition;
    }

    public static void validateConfiguration(
            String servoName,double closedPosition,double openPosition) {
        if(servoName==null||servoName.trim().isEmpty()||servoName.startsWith("REPLACE_")) {
            throw new IllegalArgumentException("replace the Servo hardware name placeholder");
        }
        if(!isServoPosition(closedPosition)||!isServoPosition(openPosition)) {
            throw new IllegalArgumentException("Servo positions must be finite values in [0,1]");
        }
        if(Double.compare(closedPosition,openPosition)==0) {
            throw new IllegalArgumentException("closed and open Servo positions must differ");
        }
    }

    public void close() {
        releaseServo.setPosition(closedPosition);
    }

    public void open() {
        releaseServo.setPosition(openPosition);
    }

    private static boolean isServoPosition(double position) {
        return Double.isFinite(position)&&position>=0.0&&position<=1.0;
    }
}
```

- [ ] **Step 4: Run focused and module tests**

Run:

```bash
./gradlew :TeamCode:testDebugUnitTest --tests '*ExampleServoHardwareTest'
./gradlew :TeamCode:testDebugUnitTest
```

Expected: both commands report `BUILD SUCCESSFUL`; the focused class contains four passing tests.

- [ ] **Step 5: Commit Task 2**

```bash
git add TeamCode/build.gradle TeamCode/src/main/java/org/firstinspires/ftc/teamcode/examples/hardware/ExampleServoHardware.java TeamCode/src/test/java/org/firstinspires/ftc/teamcode/examples/hardware/ExampleServoHardwareTest.java
git commit -m "feat: add example Servo hardware wrapper"
```

---

### Task 3: Add the disabled Servo + Pedro Auto state-machine template

**Files:**
- Create: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/examples/autonomous/ServoPedroAutoExample.java`

**Interfaces:**
- Consumes: `Constants.createFollower(HardwareMap)` and `ExampleServoHardware`.
- Produces: Driver Station example OpMode named `Servo Pedro Auto Example`, disabled until copied/configured.

- [ ] **Step 1: Create the Auto template**

```java
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
                    // ElapsedTime keeps waiting non-blocking; do not call sleep() in loop().
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

    private boolean samePosition(Pose first,Pose second) {
        return first!=null&&second!=null
                &&Double.compare(first.getX(),second.getX())==0
                &&Double.compare(first.getY(),second.getY())==0;
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

    private String describe(RuntimeException exception) {
        return exception.getClass().getSimpleName()+": "+exception.getMessage();
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
            telemetry.addData("follower busy",follower.isBusy());
            telemetry.addData("x (in)",follower.getPose().getX());
            telemetry.addData("y (in)",follower.getPose().getY());
            telemetry.addData("heading (rad)",follower.getPose().getHeading());
        }
        telemetry.addData("last servo command",lastServoCommand);
        telemetry.addData("state elapsed (s)",stateTimer.seconds());
        telemetry.update();
    }
}
```

- [ ] **Step 2: Verify safety markers and required API calls statically**

Run:

```bash
rg -n '@Disabled|CONFIGURATION_COMPLETE=false|follower\.update\(\)|follower\.isBusy\(\)|follower\.followPath\(|follower\.breakFollowing\(\)|robot\.close\(\)|robot\.open\(\)' TeamCode/src/main/java/org/firstinspires/ftc/teamcode/examples/autonomous/ServoPedroAutoExample.java
```

Expected: every marker and call appears. Confirm there is no `sleep(` call:

```bash
rg -n 'sleep\(' TeamCode/src/main/java/org/firstinspires/ftc/teamcode/examples/autonomous/ServoPedroAutoExample.java
```

Expected: no output and exit status 1.

- [ ] **Step 3: Compile and test the complete module**

Run:

```bash
./gradlew :TeamCode:testDebugUnitTest :TeamCode:assembleDebug
```

Expected: `BUILD SUCCESSFUL`; four wrapper validation tests pass; only known Java 8 target/deprecation warnings may remain.

- [ ] **Step 4: Review the diff for copied robot values and formatting errors**

Run:

```bash
git diff --check
git diff --stat
rg -n '1\.0\.9|leftFront|leftRear|rightFront|rightRear|pinpoint|pathEnd|PID|0\.1|0\.4' TeamCode/src/main/java/org/firstinspires/ftc/teamcode/examples TeamCode/src/main/java/org/firstinspires/ftc/teamcode/pedroPathing
```

Expected: `git diff --check` has no output. Any numeric match must be a range check or sentinel explanation, never a copied hardware configuration; drivetrain names, Pinpoint offsets, and PID constants must not appear.

- [ ] **Step 5: Commit Task 3**

```bash
git add TeamCode/src/main/java/org/firstinspires/ftc/teamcode/examples/autonomous/ServoPedroAutoExample.java
git commit -m "feat: add safe Servo Pedro auto example"
```

---

### Task 4: Final repository verification

**Files:**
- Verify only; no new files expected.

**Interfaces:**
- Consumes: all outputs from Tasks 1-3.
- Produces: evidence that the example compiles and the working tree contains only intended changes.

- [ ] **Step 1: Run the final Android and unit-test gate**

Run:

```bash
./gradlew :TeamCode:testDebugUnitTest :TeamCode:assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Verify history and worktree scope**

Run:

```bash
git log -4 --oneline --decorate
git status --short --branch
```

Expected: the three implementation commits follow the design commit, and no unrelated untracked or modified files are present. Do not push or deploy unless the user separately requests it.
