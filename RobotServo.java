
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class RobotServo {
    private Servo servo;
    private Telemetry telemetry;
    
    private double positionParked;      // Expected start position
    private double positionBase;        // Base position when operating.
    private double positionPerDegree;   // Usually measured. Requires scaleRange set prior to measurement.
    private double positionMin;         // Usually measured. Requires scaleRange set prior to measurement.
    private double positionMax;         // Usually measured. Requires scaleRange set prior to measurement.
    private double positionAdjust;      // Adjust closest physical mounting to square.
    private double positionLast;        // Estimated or actual position prior to last move.

    // nanoTime values are used to prevent run-away setting of servo position when under driver control. i.e,
    //   prevents servo setting being looped up to 1.0 because 400 opMode loops have occurred during the time the
    //   driver pressed up on the joystick for 1/4 second.
    private long nanoTimePerDegree;
    private long nanoTimeIncrementAllowed;   // Insure that this is initialized at or below actual System.nanoTime().
    private long nanoTimeDecrementAllowed;   // Insure that this is initialized at or below actual System.nanoTime().
    private long nanoTimeLast;               // Time of initiating last move.

    public RobotServo(Servo servo, Telemetry telemetry) {
        // Default to park and base of 0, full range of motion, 180 degrees of motion, no adjust, and
        // a very conservative (slow but safe) speed expectation.
        this(servo, telemetry, 0, 0, 0, 1, 0.00555555555, 0, 10000000);
    }

    public RobotServo(
            Servo servo, Telemetry telemetry, double positionParked, double positionBase, double positionMin, double positionMax,
            double positionPerDegree, double positionAdjust, long nanoTimePerDegree) {
        
        this.servo = servo;
        this.telemetry = telemetry;
        this.positionParked = positionParked;
        this.positionBase = positionBase;
        this.positionMin = positionMin;
        this.positionMax = positionMax;
        this.positionPerDegree = positionPerDegree;
        this.positionAdjust = positionAdjust;
        this.nanoTimePerDegree = nanoTimePerDegree;

        this.nanoTimeIncrementAllowed = Long.MIN_VALUE; // Earliest possible nanoTime to assure first move is allowed.
        this.nanoTimeDecrementAllowed = Long.MIN_VALUE; // -9223372036854775808
    }

    public void setPositionParked(double positionParked) {
        this.positionParked = positionParked;
    }

    public void setPositionBase(double positionBase) {
        this.positionBase = positionBase;
    }

    public void setPositionPerDegree(double positionPerDegree) {
        this.positionPerDegree = positionPerDegree;
    }

    public void setPositionAdjust(double positionAdjust) {
        this.positionAdjust = positionAdjust;
    }

    public void setNanoTimePerDegree(long nanoTimePerDegree) {
        this.nanoTimePerDegree = nanoTimePerDegree;
    }

    public void resetNanoTimeIncrementAllowed() {
        nanoTimeIncrementAllowed = Long.MIN_VALUE;
    }

    public void resetNanoTimeDecrementAllowed() {
        nanoTimeDecrementAllowed = Long.MIN_VALUE;
    }

    public void setDirection(Servo.Direction direction) {
        servo.setDirection(direction);
    }

    public double getPositionParked() {
        return positionParked;
    }

    public double getPositionBase() {
        return positionBase;
    }

    public double getPositionPerDegree() {
        return positionPerDegree;
    }

    public double getPositionAdjust() {
        return positionAdjust;
    }

    public long getNanoTimePerDegree() {
        return nanoTimePerDegree;
    }

    double getCurrentPosition() {
        return servo.getPosition();
    }

    public void toPark() {
        setPosition(positionParked);
    }

    public void toBase() {
        setPosition(positionBase);
    }

    // boolean positionIncrement(double degrees) {
    //     return positionIncrement(double degrees, System.nanoTime());
    // }

    // Calls to System.nanoTime are expensive, so allow already known time to be used if available.
    boolean positionIncrement(double degrees, long nanoTimeCurrent) {
        double position = servo.getPosition();
        double newPosition;
        // Position check is fast relative to time check. So do this first to avoid costly system time calls.
        if (degrees >= 0) {
            if (DoubleUtil.greaterThanOrEqual(position, positionMax)) {
                return false;
            }    // If already at max.

            if (nanoTimeCurrent < nanoTimeIncrementAllowed) {    // If finishing last move.
                telemetry.addData("nanoTimeCurrent", nanoTimeCurrent / 1000000);
                telemetry.addData("nanoTimeIncrementAllowed", nanoTimeIncrementAllowed / 1000000);
                return false;
            }
        } else {
            if (DoubleUtil.lessThanOrEqual(position, positionMin)) {
                return false;
            }    // If already at min.

            if (nanoTimeCurrent < nanoTimeDecrementAllowed) {    // If finishing last move.
                telemetry.addData("nanoTimeCurrent", nanoTimeCurrent / 1000000);
                telemetry.addData("nanoTimeDecrementAllowed", nanoTimeDecrementAllowed / 1000000);
                return false;
            }
        }

        newPosition = position - (positionPerDegree * degrees);
        setPosition(newPosition, nanoTimeCurrent);
        return true;
    }

    public void setPosition(double position) {
        setPosition(position, System.nanoTime());
    }

    public void setPosition(double position, long nanoTimeCurrent) {
        // This is the final step before moving the servo. So handle all limits and adjust here.
        position += positionAdjust;             // Add in offset for fine tuning around gear positions.
        if (position < positionMin) {
            position = positionMin;             // Limit to positionMin.
        } else if (position > positionMax) {
            position = positionMax;             // Limit to positionMax.
        }

        positionLast = positionEstimate(nanoTimeCurrent);
        if(position > positionLast) {
            nanoTimeIncrementAllowed = nanoTimeCurrent +
                    (long)((position - positionLast) / positionPerDegree) * nanoTimePerDegree;
        }

        if(position < positionLast) {
            nanoTimeDecrementAllowed = nanoTimeCurrent +
                    (long)((positionLast - position) / positionPerDegree) * nanoTimePerDegree;
        }


        nanoTimeLast = nanoTimeCurrent;
        servo.setPosition(position);
    }

    public double positionEstimate() {
        return positionEstimate(System.nanoTime());
    }

    public double positionEstimate(long nanoTimeCurrent) {
        double positionSet          = servo.getPosition();
        double movementSet          = positionSet - positionLast;

        // nanoTimeMoveRequired is estimated time required to move the movementSet distance.
        // distance set in degrees * time per degree
        // (distance as double / distance per degree) * time per degree
        long nanoTimeMoveRequired   =
                (long)(Math.abs(movementSet) / positionPerDegree * nanoTimePerDegree);
        long nanoTimeMoveActual     = nanoTimeCurrent - nanoTimeLast;
        long nanoTimeEstMoveDone    = nanoTimeLast + nanoTimeMoveRequired;

        if(nanoTimeCurrent > nanoTimeEstMoveDone) {
            return positionSet;
        } else {
            // Accurate enough. Does not handle the fact that movement involves some accelleration time.
            // timeActual / timeRequired is roughly equivalent to distanceActual / distanceRequired, so
            //   can be used as a factor to estimate distance.
            double movementActual = movementSet * ((double)nanoTimeMoveActual / nanoTimeMoveRequired);
            return positionLast + movementActual;
        }
    }
}