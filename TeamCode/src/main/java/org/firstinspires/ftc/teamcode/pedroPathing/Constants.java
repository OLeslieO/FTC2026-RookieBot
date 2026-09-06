package org.firstinspires.ftc.teamcode.pedroPathing;

import com.pedropathing.follower.Follower;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Teaching-only Pedro construction boundary.
 *
 * <p>The generic repository deliberately supplies no drivetrain or localizer. Before any real
 * drive test, replace the throwing body of {@link #createFollower(HardwareMap)} with the current
 * robot's reviewed Pedro Quickstart configuration, including both components and completed
 * localization tuning. Never copy another robot's directions, offsets, mass, PID, or constraints.
 */
public final class Constants {
    private Constants() {}

    public static Follower createFollower(HardwareMap hardwareMap) {
        throw new IllegalStateException(
                "Pedro Follower is intentionally unavailable: supply the current robot's "
                        +"complete drivetrain and localizer configuration in Constants.createFollower()"
                        +" before enabling this teaching example");
    }
}
