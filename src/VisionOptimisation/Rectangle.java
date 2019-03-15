package VisionOptimisation;

import java.io.Serializable;

/**
 * Created by Jonah on 3/18/2017.
 */
public class Rectangle implements Serializable {
    //X, Y is a corner, not in the center (which corner depends on how you use this)
    private int x, y, width, height;

    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean inBounds(int testX, int testY) {
        if (testX >= x && testX < x + width) {
            if (testY >= y && testY < y + height) {
                return true;
            }
        }
        return false;
    }

    public boolean inBounds(Rectangle testBounds) {
        if (testBounds.getX() >= x && testBounds.getX() <  x + width + testBounds.getWidth()) {
            if (testBounds.getY() >= y && testBounds.getY() < y + height + testBounds.getHeight()) {
                return true;
            }
        }
        return false;
    }
}
