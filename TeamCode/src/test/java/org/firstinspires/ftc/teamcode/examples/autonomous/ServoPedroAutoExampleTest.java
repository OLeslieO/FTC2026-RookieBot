package org.firstinspires.ftc.teamcode.examples.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import java.lang.reflect.Field;
import org.junit.Assert;
import org.junit.Test;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

public class ServoPedroAutoExampleTest {
    @Test
    public void defaultExampleRemainsDisabledAndConfigurationLocked() throws Exception {
        Field configurationComplete=
                ServoPedroAutoExample.class.getDeclaredField("CONFIGURATION_COMPLETE");
        configurationComplete.setAccessible(true);

        Assert.assertTrue(ServoPedroAutoExample.class.isAnnotationPresent(Disabled.class));
        Assert.assertFalse(configurationComplete.getBoolean(null));
        Assert.assertFalse(Constants.PEDRO_CONFIGURED);
    }
}
