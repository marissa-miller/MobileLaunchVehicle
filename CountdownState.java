package org.firstinspires.ftc.teamcode;

public enum CountdownState {
    COUNTDOWN_HOLDING(0, "holding."),
    COUNTDOWN_COUNTING(1, "counting."),
    COUNTDOWN_COMPLETE(2, "complete.");

    public final int state;
    public final String description;

    CountdownState(int state, String description) {
        this.state = state;
        this.description = description;
    }

    public CountdownState next() {
        // Clamp to the maximum index of the enum array
        return values()[Math.min(values().length - 1, ordinal() + 1)];
    }

    public CountdownState previous() {
        // Clamp to a minimum index of 0
        return values()[Math.max(0, ordinal() - 1)];
    }
    
    public int getState() {
        return state;
    }

    public String getDescription() {
        return description;
    }
}
