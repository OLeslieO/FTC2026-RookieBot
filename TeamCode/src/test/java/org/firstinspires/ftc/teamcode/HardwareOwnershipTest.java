package org.firstinspires.ftc.teamcode;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Test;

/** 防止新增 Auto/TeleOp 时又把硬件查找或 Pedro 硬件构造散落到别的文件。 */
public class HardwareOwnershipTest {
    @Test
    public void hardwareInitializationStaysInHardwares() throws Exception {
        Path root=Paths.get("src/main/java/org/firstinspires/ftc/teamcode");
        Pattern initialization=Pattern.compile(
                "hardwareMap\\s*\\.\\s*(get\\s*\\(|[a-zA-Z]+\\s*\\.\\s*get\\s*\\()"
                        +"|new\\s+(FollowerBuilder|Mecanum|PinpointLocalizer)\\s*\\(");
        List<Path> sources;
        try(Stream<Path> files=Files.walk(root)) {
            sources=files.filter(path->path.toString().endsWith(".java"))
                    .filter(path->!path.getFileName().toString().equals("Hardwares.java"))
                    .collect(Collectors.toList());
        }
        Assert.assertFalse("No TeamCode sources checked",sources.isEmpty());
        for(Path source:sources) {
            String code=new String(Files.readAllBytes(source),StandardCharsets.UTF_8);
            Assert.assertFalse("Move hardware initialization to Hardwares: "+source,
                    initialization.matcher(code).find());
        }
    }
}
