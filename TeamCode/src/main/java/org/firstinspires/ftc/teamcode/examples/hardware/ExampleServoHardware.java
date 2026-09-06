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
        if(closedPosition==openPosition) {
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
