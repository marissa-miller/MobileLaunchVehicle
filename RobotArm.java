package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class RobotArm {
    public RobotServo waist;
    public RobotServo shoulder;

    public RobotArm(HardwareMap hardwareMap, Telemetry telemetry) {
        this.waist = new RobotServo(
                hardwareMap.servo.get("serv4"),
                telemetry, 
                0.5, 0.5, 0.0, 1.0, 0.00175, 0.0, 5000000L);

        this.shoulder = new RobotServo(
                hardwareMap.servo.get("serv5"),
                telemetry, 
                0.4563, 0.21885, 0.16, 0.5038, 0.003166, 0.0, 7500000L);

                // RobotServo elbow = new RobotServo(
                //         hardwareMap.servo.get("serv3"), 0.5, 0.315, 0, 1, 0.00175, -0.0025, (long)5000000);

                // RobotServo forearm = new RobotServo(
                //         hardwareMap.servo.get("serv2"), 0.5, 0.315, 0, 1, 0.00175, -0.0025, 5000000);

                // RobotServo wrist = new RobotServo(
                //         hardwareMap.servo.get("serv <Need New Hub (more servos) for this> "), 0.5, 0.315, 0, 1, 0.00175, -0.0025, 5000000);

                // RobotServo claw = new RobotServo(
                //         hardwareMap.servo.get("serv1"), 0.5, 0.315, 0, 1, 0.00175, -0.0025, 5000000);
    }
}