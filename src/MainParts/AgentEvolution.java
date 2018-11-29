package MainParts;

import processing.core.PApplet;
import processing.event.MouseEvent;

/**
 * Created by loshaderj16 on 3/13/2017.
 */
public class AgentEvolution extends PApplet{

    private GameManager mainGame;

    public void setup() {
        mainGame = new GameManager(this);
        background(0);
        surface.setResizable(true);
    }

    public void settings() {
        size(720, 480);
        smooth(8);
//        noSmooth();
    }

    public void draw() {
        mainGame.draw();
    }

    public void mousePressed() {
        mainGame.mousePressed();
    }

    public void keyPressed() {
        mainGame.keyPressed();
    }

    public void mouseWheel(MouseEvent event) {
        mainGame.mouseWheel(event);
    }

    public static void main(String[] args) {
        PApplet.main("MainParts.AgentEvolution");
    }
}
