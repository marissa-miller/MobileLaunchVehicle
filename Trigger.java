public class Trigger {
        private boolean currentState;
        private boolean lastState;

        Trigger(boolean currentState) {
            // Initialize both with currentState, so no action triggered on creation.
            this.currentState = currentState;
            this.lastState = this.currentState;
        }

        public int getChange(boolean currentState) {
            if (currentState != lastState) {
                lastState = currentState;
                if (currentState) {     // 0: Changed to false, 1: Changed to true
                    return 1;
                } else {
                    return 0;
                }
            } else {
                return -1;              // No change from last check
            }
        }
    }