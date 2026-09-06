package org.firstinspires.ftc.teamcode;

import org.junit.Assert;
import org.junit.Test;

public class ServoAngleConversionTest {
    @Test
    public void mapsEndpointsAndQuarterTurnsWithoutWrapping() {
        Assert.assertEquals(0.0,Hardwares.servoAngleToPosition(0.0),1e-12);
        Assert.assertEquals(0.25,Hardwares.servoAngleToPosition(90.0),1e-12);
        Assert.assertEquals(0.5,Hardwares.servoAngleToPosition(180.0),1e-12);
        Assert.assertEquals(0.75,Hardwares.servoAngleToPosition(270.0),1e-12);
        Assert.assertEquals(1.0,Hardwares.servoAngleToPosition(360.0),1e-12);
    }

    @Test
    public void preservesFractionalAngles() {
        Assert.assertEquals(0.0625,Hardwares.servoAngleToPosition(22.5),1e-12);
    }

    @Test
    public void rejectsInvalidAnglesBeforeTheyReachTheServo() {
        double[] invalidAngles={-0.01,360.01,Double.NaN,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY};
        for(double angle:invalidAngles) {
            Assert.assertThrows(IllegalArgumentException.class,()->Hardwares.servoAngleToPosition(angle));
        }
    }
}
