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
