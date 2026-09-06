package org.firstinspires.ftc.teamcode.pedroPathing;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ConstantsTest {
    @Test
    public void rejectsFollowerCreationUntilRobotConfigurationIsSupplied() {
        IllegalStateException exception=assertThrows(
                IllegalStateException.class,()->Constants.createFollower(null));

        assertTrue(exception.getMessage().contains("drivetrain"));
        assertTrue(exception.getMessage().contains("localizer"));
    }
}
