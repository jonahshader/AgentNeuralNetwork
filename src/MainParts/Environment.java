package MainParts;

/**
 * Created by Jonah on 4/7/2017.
 */
public class Environment {
    public final static float DAY_NIGHT_CYCLE_LENGTH = 10000.0f; //10000
    public final static float MAX_BRIGHTNESS = 180f;

    float brightness = 127.5f;

    public void calculateEnvironment(int rawFrameCount) {
        calculateDayTime(rawFrameCount);
    }

    public float getBrightness() {
        return brightness;
    }

    public float getVisibility() {
//        System.out.println(brightness / MAX_BRIGHTNESS);
        return brightness / MAX_BRIGHTNESS;
//        return 1f;
    }

    private void calculateDayTime(int rawFrameCount) {
        brightness = (float) ((Math.sin((rawFrameCount / DAY_NIGHT_CYCLE_LENGTH) * Math.PI) + 1.0) / 2.0);
        brightness = (float) Math.pow(brightness, 0.2);
        brightness *= MAX_BRIGHTNESS;
    }
}
