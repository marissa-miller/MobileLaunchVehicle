package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.robotcore.external.android.AndroidTextToSpeech;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

public class Voice {
    private AndroidTextToSpeech androidTextToSpeech;
    private LinearOpMode opMode; // Store the OpMode reference

    public Voice(LinearOpMode opMode) {
        this.opMode = opMode;
        this.androidTextToSpeech = new AndroidTextToSpeech();
        androidTextToSpeech.initialize();
        androidTextToSpeech.setLanguageAndCountry("en", "US");
        opMode.sleep(500); // Use the OpMode's safe sleep
        speak("System started and paused.", true);
    }

    public void speak(String text) {
        speak(text, false);
    }

    public void speak(String text, boolean bWait) {
        boolean bStillSpeaking = true;
        androidTextToSpeech.speak(text);
        opMode.sleep(50); // Use the OpMode's safe sleep
        if(bWait) {
            do {
                bStillSpeaking = androidTextToSpeech.isSpeaking();
            } while (bStillSpeaking);
        }
    }
}