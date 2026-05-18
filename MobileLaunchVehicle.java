package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.util.Range;

// Basic OpMode Requirements
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

// Audio and Text-to-speech
import com.qualcomm.ftccommon.SoundPlayer;
import org.firstinspires.ftc.robotcore.external.android.AndroidSoundPool;
import org.firstinspires.ftc.robotcore.external.android.AndroidTextToSpeech;

// Inertial Measurement Unit
import com.qualcomm.hardware.bosch.BNO055IMU;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;

// Motion
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;

@TeleOp(name = "MobileLaunchVehicle", group = "")
public class MobileLaunchVehicle extends LinearOpMode {
    private final static boolean DEBUG = false;
    private final static boolean VERBOSE = false;
    private final static boolean FULL_TIMING = false;

    private void reportSequenceTime(long zeroTime) {
    }

    //Time
    // private LocalTime zeroHour = LocalTime.now();
    // private Duration  countDownClock = Duration.of(30,SECONDS);

    // Audio
    private Voice voice; // Initialized in runOpMode() to support safer 'sleep' method.
    private AndroidSoundPool androidSoundPool = new AndroidSoundPool();

    private Countdown countdown = new Countdown(70);

    // private List<Recognition> recognitions;
    // private double goldMineralX;
    // private double silverMineral1X;
    // private double silverMineral2X;
    // // -1: None, 1: Left, 2: Center, 3: Right (TODO: Switch to enum for cube found)
    // private int lastCubeFound = -1;
    // private int thisCubeFound = -1;
    // //    }

    // IMU
    private BNO055IMU imu;

    Speed speed = Speed.SPEED_TWO;

    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor frontLeft;
    private DcMotor frontRight;

    // Controls
    Trigger oneLeftStickBtn     = new Trigger(false);   // .
    Trigger oneRightStickBtn    = new Trigger(false);   // .
    Trigger oneA                = new Trigger(false);   // .
    Trigger oneB                = new Trigger(false);   // .
    Trigger oneX                = new Trigger(false);   //
    Trigger oneY                = new Trigger(false);   //
    Trigger oneLeftBumper       = new Trigger(false);   // Deploy arm.
    Trigger oneRightBumper      = new Trigger(false);   // Park arm.
    Trigger oneBack             = new Trigger(false);   // Arm demo
    Trigger oneDPadLeft         = new Trigger(false);   // .
    Trigger oneDPadRight        = new Trigger(false);   // .
    Trigger oneDPadUp           = new Trigger(false);   //  Speed up.
    Trigger oneDPadDown         = new Trigger(false);   //  Speed down.

    int launchSequenceStep = -1;

    public int abortLaunch() {
        countdown.reset();      // Also handles CountdownStatus.
        if(launchSequenceStep != -1) {
            launchSequenceStep = -1;
            voice.speak("Mission Abort! Aborting launch.", true);
        }
        return -1;
    }

    public int goNoGoHold(String decisionMaker, String decisionSubject) {
        if (gamepad1.a && gamepad1.b) {
            return abortLaunch();
        } else if (gamepad1.left_bumper && gamepad1.right_bumper && (oneX.getChange(gamepad1.x) == 1)) {
            if(countdown.getCountdownState() == CountdownState.COUNTDOWN_COUNTING) {
                voice.speak(decisionMaker + " reports, go for launch.", true);
            } else if (countdown.getCountdownState() == CountdownState.COUNTDOWN_HOLDING) {
                countdown.resumeCountdown(); // Properly encapsulated
                voice.speak(decisionMaker + " reports, go for launch. Resuming countdown.", true);
            }
            return 1;
        } else if (oneY.getChange(gamepad1.y) == 1) {
            if(countdown.getCountdownState() == CountdownState.COUNTDOWN_COUNTING) {
                countdown.holdCountdown(); // Properly encapsulated
                voice.speak(decisionSubject + " is no go. Hold countdown.", true);
                countdown.setFlag(200);
            }
        }
        return 0;
    } 

    @Override
    public void runOpMode() {
        // Audio
        voice = new Voice(this); // Initialize here to use OpMode's sleep method, which is safer (vs. Thread.sleep())
        // androidTextToSpeech.initialize();
        androidSoundPool.initialize(SoundPlayer.getInstance());

        // IMU
        BNO055IMU.Parameters imuParameters;
        Orientation angles;
        Acceleration gravity;
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imuParameters = new BNO055IMU.Parameters();
        imuParameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imuParameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        imuParameters.loggingEnabled = false;
        imu.initialize(imuParameters);

        // Motion
        double basePower;
        double leftPower;
        double rightPower;
        double turnPower = 0;

        // Arm Servos
        RobotArm robotArm = new RobotArm(hardwareMap, telemetry);
        
        RobotServo servLaunchMaster = new RobotServo(
                hardwareMap.servo.get("serv0"), telemetry,
                0.20, 0.03, 0.0, 1.0, 0.00175, -0.03, (long)5000000);
        RobotServo servLaunch1 = new RobotServo(
                hardwareMap.servo.get("serv1"), telemetry,
                0.20, 0.03, 0.0, 1.0, 0.00175, -0.00, (long)5000000);
        RobotServo servLaunch2 = new RobotServo(
                hardwareMap.servo.get("serv2"), telemetry,
                0.20, 0.03, 0.0, 1.0, 0.00175, -0.03, (long)5000000);
        RobotServo servLaunch3 = new RobotServo(
                hardwareMap.servo.get("serv3"), telemetry,
                0.20, 0.03, 0.0, 1.0, 0.00175, 0.01, (long)5000000);

        // Should park on init, but make sure here.
        servLaunchMaster.toPark();
        servLaunch1.toPark();
        servLaunch2.toPark();
        servLaunch3.toPark();


        // Motors
        rearLeft = hardwareMap.dcMotor.get("rearLeft");
        rearRight = hardwareMap.dcMotor.get("rearRight");
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");

        // Timing
        long iterationCount = 0;
        long iterationMaxCount = 10000;
        long nanoTimeLoopStart;             // timestamp on beginning to loop.
        long nanoTimeIterationStart;        // timestamp on beginning of each loop.
        long nanoTimeIterationEnd = 0;          // Solves issues on min/max tracking, and allows exclusion of timing code.
        long nanoTimeIterationDuration = 0;
        long nanoTimeIterationDurationSum = 0;
        long nanoTimeIterationDurationAvg = 0;
        long nanoTimeIterationDurationMin = 0;
        long nanoTimeIterationDurationMax = 0;
        long nanoTimeCalibrationDuration = 0;
        long nanoTimeCalibrationSum = 0;
        long nanoTimeCalibrationAvg = 0;
        long nanoTimeCalibrationMin = 0;
        long nanoTimeCalibrationMax = 0;

        long countdownStart = 0;
        // long countdownDuration = 6400;

        long timeNextCubeCheck = 0;   // To avoid looking every time.

        long countdownTimer = 30000;        // Milliseconds
        int decision = -1;
        int lastSecondReported = 0;
        int currentSecond = 0;

        waitForStart();

        androidSoundPool.setVolume(1F);
        androidSoundPool.setRate(2F);
        androidSoundPool.setLoop(3);
        // Play sound here to test.

//        androidTextToSpeech.setLanguageAndCountry("en", "US");
//        androidTextToSpeech.speak("Let's roll!");

        rearLeft.setDirection(DcMotor.Direction.REVERSE);
        rearRight.setDirection(DcMotor.Direction.FORWARD);
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        frontRight.setDirection(DcMotor.Direction.FORWARD);

        // RUN_WITHOUT_ENCODER is the least complex and least sophisticated mode. It causes the motors to simply be
        // controlled by power input to them. Note that 50% power does not result in 50% speed or torque.
        // That said, this is most reliable in testing. Encoders seem to fail often.
        //
        // TO DO: Looks like work has been done on the encoders code. Maybe try again?
        rearLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rearRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        // RUN_USING_ENCODER causes the power value to translate to a speed. Need to test. I am not sure why this is
        // not the norm. It should be more controllable than RUN_WITHOUT_CONTROLLER.
        // rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        // rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        // frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        // frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // RUN_TO_POSITION uses the encoders to move based on encoder count. I.e. distance travelled vs. speed. It
        // automatically slows and corrects if working properly.
        //    rearLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);    // Very importand in this mode.
        //    rearLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        //    rearLeft.setTargetPosition(some_encoder_value);
        //    rearLeft.setPower(some_power);
        //    int position = rearLeft.getCurrentPosition();
        //    telemetry.addData("Encoder Position", position);

        // ZeroPowerBehavior BRAKE or FLOAT.
        rearLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rearRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        if (opModeIsActive()) {
            nanoTimeLoopStart = System.nanoTime();   // Initial loop time
            boolean firstLoop = true;

            while (opModeIsActive()) {
                nanoTimeIterationStart = System.nanoTime();

                if(firstLoop) {
                    firstLoop = false;
                    voice.speak("Active. Awaiting launch sequence start.", true);
                }

                // ********************************************************************
                // Speed:
                // ********************************************************************
                if (launchSequenceStep == -1) {
                    if (oneDPadUp.getChange(gamepad1.dpad_up) == 1 && speed.getSpeed() < 5) {
                        speed = speed.next();
                        voice.speak(speed.getDescription());
                    }

                    if (oneDPadDown.getChange(gamepad1.dpad_down) == 1 && speed.getSpeed() > 1) {
                        speed = speed.previous();
                        voice.speak(speed.getDescription());
                    }
                }

                // ********************************************************************
                // Drive: For compatability with inflexible "RUN_WITH_ENCODERS",
                //   handle wheel reversal and odd y-axis values must be handled
                //   in code (vs. via physical wiring).
                // ********************************************************************
                if (launchSequenceStep == -1) {          // if not in launch sequence
                    //basePower = gamepad1.left_stick_y;    This was used for arcade drive
                    //turnPower = gamepad1.right_stick_x;
                    
                    leftPower = gamepad1.left_stick_y;
                    rightPower = gamepad1.right_stick_y;

                      
                } else {                                // disable drive motion if in launch sequence
                    //basePower = turnPower = 0;
                    
                    leftPower = rightPower = 0;
                }

                //leftPower = basePower + turnPower;    This was used for arcade drive
                //rightPower = basePower - turnPower;

                rearLeft.setPower(leftPower * speed.getPowerFactor());
                frontLeft.setPower(leftPower * speed.getPowerFactor());
                rearRight.setPower(rightPower * speed.getPowerFactor());
                frontRight.setPower(rightPower * speed.getPowerFactor());

                // ********************************************************************
                // Multi-axis Arm:
                //   All directions relative to rear of the robot, from the robot's perspective.
                // ********************************************************************
// Disable manual arm control for now.
//                if (gamepad1.right_stick_x != 0) {
//                    robotArm.waist.positionIncrement(gamepad1.right_stick_x, nanoTimeIterationStart);
//                }
//                if (gamepad1.right_stick_y != 0) {
//                    robotArm.shoulder.positionIncrement(gamepad1.right_stick_y, nanoTimeIterationStart);
//                }
//
//                if (oneRightStickBtn.getChange(gamepad1.right_stick_button) == 1 &&
//                        launchSequenceStep == -1) {
//                    if (gamepad1.right_stick_y < -0.3) {
//                        robotArm.waist.toPark();
//                        robotArm.shoulder.toPark();
//                        voice.speak("Standing down.");
//                    } else if (gamepad1.right_stick_y > 0.3) {
//                        robotArm.waist.toBase();
//                        robotArm.shoulder.toBase();
//                        voice.speak("Deploying.");
//                    } else {
//                        // mid thing here.
//                    }
//                }

                telemetry.addData("Waist", robotArm.waist.getCurrentPosition());
                telemetry.addData("Shoulder", robotArm.shoulder.getCurrentPosition());

                // ********************************************************************
                // Launch Sequence
                // ********************************************************************
                switch (launchSequenceStep) {
                    case -1:    // Not started
                        if (gamepad1.left_bumper && gamepad1.right_bumper && (oneX.getChange(gamepad1.x) == 1)) {
                            countdown.startCountdown();
                            String message = "Launch sequence started at" + countdown.getTimeTStatement();
                            voice.speak(message, true);
                            countdown.setFlag(700);

                            launchSequenceStep++;
                            // launchSequenceStep = 15;
                        }
                        break;
                    case 0:
                        if(countdown.getFlag()) {
                            voice.speak("Launch computer active.", true);
                            countdown.setFlag(700);
                            launchSequenceStep++;
                        }
                        break;
                    case 1:
                        if(countdown.getFlag()) {
                            voice.speak("Inertial measurement unit active.", true);
                            countdown.setFlag(700);
                            launchSequenceStep++;
                        }
                        break;
                    case 2:
                        if(countdown.getFlag()) {
                            voice.speak("Clear launch area.", true);
                            countdown.setFlag(1);
                            launchSequenceStep++;
                        }
                        break;
                    case 3:
                        if(countdown.getFlag()) {
                            voice.speak("Clear launch range.", true);
                            countdown.setFlag(700);
                            launchSequenceStep++;
                        }
                        break;
                    case 4:
                        if(countdown.getFlag()) {
                            voice.speak(countdown.getTimeTStatement(), true);
                            countdown.setFlag(200);
                            launchSequenceStep++;
                        }
                        break;
                    case 5:
                        if(countdown.getFlag()) {
                            voice.speak("Range controller, report status.", true);
                            launchSequenceStep++;
                            countdown.setFlag(200);
                        }
                        break;
                    case 6:
                        decision = goNoGoHold("Range controller", "Range");
                        if (decision == 1) {
                            launchSequenceStep++;
                        } else if(decision == -1) {
                            servLaunchMaster.toPark();
                            servLaunch1.toPark();
                            servLaunch2.toPark();
                            servLaunch3.toPark();                        }
                        break;
                    case 7:
                        robotArm.waist.toBase();
                        robotArm.shoulder.toBase();
                        voice.speak("Missiles to vertical. ", true);
                        countdown.setFlag(3000);
                        launchSequenceStep++;
                        break;
                    case 8:     // TO DO: Add missile azimuth and elevation report here.
                        if(countdown.getFlag()) {
                            voice.speak(countdown.getTimeTStatement(), true);
                            countdown.setFlag(1000);
                            launchSequenceStep++;
                        }
                        break;
                    case 9:
                        voice.speak("Targeting controller, report status.", true);
                        launchSequenceStep++;
                        break;
                    case 10:
                        decision = goNoGoHold("Targeting controller", "Targeting");
                        if (decision == 1) {
                            launchSequenceStep++;
                        } else if(decision == -1) {
                            servLaunchMaster.toPark();
                            servLaunch1.toPark();
                            servLaunch2.toPark();
                            servLaunch3.toPark();                        }
                        break;
                    case 11:
                        servLaunchMaster.toBase();
                        voice.speak("Independent launch power enabled. ", true);
                        countdown.setFlag(700);
                        launchSequenceStep++;
                        break;
                    case 12:
                        if(countdown.getFlag()) {
                            voice.speak(countdown.getTimeTStatement(), true);
                            countdown.setFlag(1000);
                            launchSequenceStep++;
                        }
                        break;
                    case 13:
                        voice.speak("Launch director, report final launch status.", true);
                        launchSequenceStep++;
                        break;

                    case 14:
                        decision = goNoGoHold("Launch director", "Final launch");
                        if (decision == 1) {
                            launchSequenceStep++;
                        } else if(decision == -1) {
                            servLaunchMaster.toPark();
                            servLaunch1.toPark();
                            servLaunch2.toPark();
                            servLaunch3.toPark();                        }
                        break;
                    case 15:
                        if(countdown.getTimeT() >= 20) {
                            if(countdown.getTimeT() % 10 == 0) {
                                voice.speak(countdown.getTimeTStatement(), true);
                            }
                        } else {
                            countdown.setFlag(200);
                            launchSequenceStep++;
                        }
                        break;
                    case 16:
                        if(countdown.getFlag()) {
                            voice.speak("Launch director, maintain override control.",
                                    true);
                            launchSequenceStep++;
                        }
                        break;
                    case 17:
                        if(countdown.getTimeT() >= 10) {
                            if(countdown.getTimeT() % 5 == 0) {
                                voice.speak(countdown.getTimeTStatement(), true);
                            }
                        } else {

                            lastSecondReported = 10;    // Not really, but it works.
                            launchSequenceStep++;
                        }
                        break;
                    case 18:
                        while(countdown.getTimeT() > 1) {
                            if(countdown.getTimeT() < lastSecondReported) {
                                voice.speak(countdown.getTimeTStatement(), true);
                                lastSecondReported = countdown.getTimeT();
                            }
                        }
                        launchSequenceStep++;

//                        currentSecond = countdown.getTimeT();
//                        if((currentSecond > 1) && (lastSecondReported > currentSecond)) {
//                             = currentSecond;
//                            sleep(50);
//                        } else {
//
//                        }
                        break;
                    case 19:
                        // If between T-minus 0.2 seconds and T-plus 0.5 seconds, allow launch.
                        if(countdown.getNanoTimeT() < 200000000) {
                            if (gamepad1.a && gamepad1.b) {
                                abortLaunch();
                            } else if (gamepad1.left_bumper && gamepad1.right_bumper && gamepad1.x) {
                                servLaunch1.toBase();
                                servLaunch2.toBase();
                                servLaunch3.toBase();
                                sleep(2000);
                                launchSequenceStep++;
                            }
                        }
                        break;
                    case 20:
                        servLaunchMaster.toPark();
                        servLaunch1.toPark();
                        servLaunch2.toPark();
                        servLaunch3.toPark();
                        sleep(15000);
                        launchSequenceStep++;
                        break;
                    case 21:
                        voice.speak("Launch director confirm liftoff of all missiles.", true);
                        launchSequenceStep++;
                        break;
                    case 22:
                        if (gamepad1.a && gamepad1.b) {  // Abort
                            voice.speak("Anomoly reported. Missiles remain vertical for safety.", true);
                            abortLaunch();
                        } else if (gamepad1.left_bumper && gamepad1.right_bumper && (oneX.getChange(gamepad1.x) == 1)) {
                            robotArm.waist.toPark();
                            robotArm.shoulder.toPark();
                            voice.speak("Launch director confirms liftoff of all missles. Returning to drive mode.", true);
                            launchSequenceStep = -1;
                        }
                        break;
                }

                // ********************************************************************
                // Timing
                // ********************************************************************
                if (FULL_TIMING) {
                    nanoTimeIterationEnd = System.nanoTime();
                    nanoTimeCalibrationDuration = System.nanoTime() - nanoTimeIterationEnd; // Overhead of getting nanoTime.
                    nanoTimeIterationDuration = nanoTimeIterationEnd - nanoTimeIterationStart;
                    if (iterationCount < iterationMaxCount) {
                        nanoTimeIterationDurationSum += nanoTimeIterationDuration;
                        nanoTimeCalibrationSum += nanoTimeCalibrationDuration;
                        if (nanoTimeIterationDuration < nanoTimeIterationDurationMin) {
                            nanoTimeIterationDurationMin = nanoTimeIterationDuration;
                        }
                        if (nanoTimeIterationDuration > nanoTimeIterationDurationMax) {
                            nanoTimeIterationDurationMax = nanoTimeIterationDuration;
                        }
                        if (nanoTimeCalibrationDuration < nanoTimeCalibrationMin) {
                            nanoTimeCalibrationMin = nanoTimeCalibrationDuration;
                        }
                        if (nanoTimeCalibrationDuration > nanoTimeCalibrationMax) {
                            nanoTimeCalibrationMax = nanoTimeCalibrationDuration;
                        }
                        iterationCount++;
                    } else {
                        nanoTimeIterationDurationAvg = nanoTimeIterationDurationSum / iterationMaxCount;
                        nanoTimeCalibrationAvg = nanoTimeCalibrationSum / iterationMaxCount;
                        telemetry.addData("Avg Loop MS", (double) nanoTimeIterationDurationAvg / 1000000);
                        telemetry.addData("Min Loop MS", (double) nanoTimeIterationDurationMin / 1000000);
                        telemetry.addData("Max Loop MS", (double) nanoTimeIterationDurationMax / 1000000);
                        telemetry.addData("Avg Cal MS", (double) nanoTimeCalibrationAvg / 1000000);
                        telemetry.addData("Min Cal MS", (double) nanoTimeCalibrationMin / 1000000);
                        telemetry.addData("Max Cal MS", (double) nanoTimeCalibrationMax / 1000000);
                        telemetry.update();
                        iterationCount = 0;
                        // Reset all for new series of iterations to average, etc.
                        // No need to clear averages, as last loop's avg is useful pending each new one.
                        nanoTimeIterationDurationSum = nanoTimeCalibrationSum = 0;
                        nanoTimeIterationDurationMin = nanoTimeIterationDurationMax = nanoTimeIterationDuration;
                        nanoTimeCalibrationMin = nanoTimeCalibrationMax = nanoTimeCalibrationDuration;
                    }
                }

                // ********************************************************************
                // Telemetry Update:
                // ********************************************************************
                // angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
                // gravity = imu.getGravity();
                // telemetry.addData("rot about Z", angles.firstAngle);
                // telemetry.addData("rot about Y", angles.secondAngle);
                // telemetry.addData("rot about X", angles.thirdAngle);
                // telemetry.addData("gravity (Z)", gravity.zAccel);
                // telemetry.addData("gravity (Y)", gravity.yAccel);
                // telemetry.addData("gravity (X)", gravity.xAccel);

                telemetry.update();
            }

        }

//        androidTextToSpeech.close();
        androidSoundPool.close();

        sleep(1000);    // Give things a chance to finish... We have no feedback on servo positions. (This doesn't work.)
    }
}