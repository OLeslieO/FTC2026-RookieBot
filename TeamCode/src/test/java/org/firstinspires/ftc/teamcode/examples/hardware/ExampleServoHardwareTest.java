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

    @Test
    public void rejectsSignedZeroPositionsAsIdentical() {
        assertThrows(IllegalArgumentException.class,()->
                ExampleServoHardware.validateConfiguration("releaseServo",-0.0,0.0));
        assertThrows(IllegalArgumentException.class,()->
                ExampleServoHardware.validateConfiguration("releaseServo",0.0,-0.0));
    }
}
