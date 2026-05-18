package org.firstinspires.ftc.teamcode;

public enum Speed {
    SPEED_ZERO(0, 0, "Zero."),
    SPEED_ONE(1, 0.20, "One."),
    SPEED_TWO(2, 0.40, "Two."),
    SPEED_THREE(3, 0.60, "Three."),
    SPEED_FOUR(4, 0.80, "Four."),
    SPEED_FIVE(5, 1.00, "Five.");

    private final int speed;
    private final double powerFactor;
    private final String description;

    Speed(int speed, double powerFactor, String description) {
        this.speed = speed;
        this.powerFactor = powerFactor;
        this.description = description;
    }

    public Speed next() {
        // Clamp to the maximum index of the enum array
        return values()[Math.min(values().length - 1, ordinal() + 1)];
    }

    public Speed previous() {
        // Cannot shift down into ZERO.
        if (this == SPEED_ZERO || this == SPEED_ONE) {
            return this;
        }
        return values()[ordinal() - 1];
    }

    public int getSpeed() {
        return speed;
    }

    public double getPowerFactor() {
        return powerFactor;
    }

    public String getDescription() {
        return description;
    }
}