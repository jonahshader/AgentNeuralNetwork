package MainParts;

import java.io.Serializable;

/**
 * Created by Jonah on 4/7/2017.
 */
public class Environment implements Serializable {
    public final static boolean DAY_NIGHT_CYCLE_ENABLED = false;
    public final static float DAY_NIGHT_CYCLE_LENGTH = 10000.0f; //10000
    public final static float MAX_BRIGHTNESS = 180f;

    float brightness = 127.5f;

    public Environment(long currentFrame) {
        calculateEnvironment(currentFrame);
    }

    //may contain other enviornmental things in the future
    public void calculateEnvironment(long rawFrameCount) {
        calculateDayTime(rawFrameCount);
    }

    public float getBrightness() {
        return brightness;
    }

    public float getVisibility() {
        return brightness / MAX_BRIGHTNESS;
    }

    private void calculateDayTime(long rawFrameCount) {
        if (DAY_NIGHT_CYCLE_ENABLED) {
            brightness = (float) ((Math.sin((rawFrameCount / DAY_NIGHT_CYCLE_LENGTH) * Math.PI) + 1.0) / 2.0);
            brightness = (float) Math.pow(brightness, 0.2);
            brightness *= MAX_BRIGHTNESS;
        } else {
            brightness = MAX_BRIGHTNESS;
        }

    }
}
