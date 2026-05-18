public class Countdown {
        private CountdownState countdownState;
        private int duration;
        private long nanoTimeT; // Launch time
        private long nanoTimeRemaining;
        private long nanoTimeFlag;
        private final long billion = 1000000000;

        Countdown(int duration) {       // duration in seconds until launch
            this.duration = duration;
            reset();
        }

        public void reset() {
            this.nanoTimeT      = 0;
            this.nanoTimeRemaining   = 0;
            this.countdownState = CountdownState.COUNTDOWN_HOLDING;
            this.nanoTimeFlag   = 0;
        }

        public boolean startCountdown() {
            nanoTimeRemaining = duration * billion;
            return resumeCountdown();
        }

        public boolean resumeCountdown() {
            if(countdownState == CountdownState.COUNTDOWN_HOLDING) {
                nanoTimeT = System.nanoTime() + nanoTimeRemaining;
                countdownState = CountdownState.COUNTDOWN_COUNTING;
                return true;
            } else {
                return false;
            }
        }

        public boolean holdCountdown() {
            if(countdownState == CountdownState.COUNTDOWN_COUNTING) {
                nanoTimeRemaining = nanoTimeT - System.nanoTime();
                if (nanoTimeRemaining < 0) {
                    // ToDo: This is arguably an error state. Should we do something more?
                    nanoTimeRemaining = 0;
                }
                countdownState = CountdownState.COUNTDOWN_HOLDING;
                return true;
            } else {
                return false;
            }
        }

        public String getTimeTStatement() {
            return getTimeTStatement(0);
        }

        public String getTimeTStatement(int offset) {
            int tSeconds = getTimeT() - offset;  // offset to account for speaking/reporting delay.
            if (tSeconds > 9) {
                return " T minus " + Integer.toString(tSeconds) + " seconds and " + countdownState.description;
            } else if (tSeconds < 0) {
                return " T plus " + Integer.toString(Math.abs(tSeconds)) + " seconds and " + countdownState.description;
            } else {
                return Long.toString(tSeconds);
            }
        }

        public int getTimeT() {
            return (int)Math.round((float)getNanoTimeT() / billion);
        }

        public long getNanoTimeT() {
            return nanoTimeT - System.nanoTime();
        }

        public void setFlag(int flagMilliseconds) {
            nanoTimeFlag = System.nanoTime() + (flagMilliseconds * 1000000);
        }

        public boolean getFlag() {
            if(System.nanoTime() > nanoTimeFlag && nanoTimeFlag > 0) {
                nanoTimeFlag = 0;
                return true;
            } else {
                return false;
            }
        }

}
