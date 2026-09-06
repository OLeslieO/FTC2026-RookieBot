package org.firstinspires.ftc.teamcode.examples.autonomous;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import java.lang.reflect.Field;
import org.junit.Test;

public class ServoPedroAutoExampleTest {
    @Test
    public void defaultExampleRemainsDisabledAndConfigurationLocked() throws Exception {
        Field configurationComplete=
                ServoPedroAutoExample.class.getDeclaredField("CONFIGURATION_COMPLETE");
        configurationComplete.setAccessible(true);

        assertTrue(ServoPedroAutoExample.class.isAnnotationPresent(Disabled.class));
        assertFalse(configurationComplete.getBoolean(null));
    }

    @Test
    public void treatsSignedZeroPoseCoordinatesAsTheSamePosition() {
        assertTrue(ServoPedroAutoExample.samePosition(
                new Pose(-0.0,4.0,0.0),new Pose(0.0,4.0,1.0)));
        assertTrue(ServoPedroAutoExample.samePosition(
                new Pose(3.0,-0.0,0.0),new Pose(3.0,0.0,1.0)));
    }

    @Test
    public void followerTelemetryFailuresReturnUnavailableSnapshots() {
        ServoPedroAutoExample.FollowerTelemetrySnapshot busyFailure=
                ServoPedroAutoExample.captureFollowerTelemetry(
                        ()->{
                            throw new IllegalStateException("follower offline");
                        },()->new Pose());
        ServoPedroAutoExample.FollowerTelemetrySnapshot poseFailure=
                ServoPedroAutoExample.captureFollowerTelemetry(
                        ()->false,()->{
                            throw new IllegalStateException("localizer offline");
                        });

        assertFalse(busyFailure.isAvailable());
        assertTrue(busyFailure.getFailure().contains("follower offline"));
        assertFalse(poseFailure.isAvailable());
        assertTrue(poseFailure.getFailure().contains("localizer offline"));
    }
}
