package GamePieces;

import MainParts.AgentEvolution;
import MainParts.GameManager;
import MainParts.Modes;
import VisionOptimisation.VisionOptimiser;

import java.util.ArrayList;
import java.util.Random;

import static NeuralNetStuff.NeuralNetwork.ranRange;

/**
 * Created by Jonah on 7/6/2017.
 */
public class Spike {
    final static int BASE_COLOR_RED = 230;
    final static int BASE_COLOR_GREEN = 10;
    final static int BASE_COLOR_BLUE = 25;
    final static double BASE_COLOR_VARIANCE = 0.1f; //Percentage
    final static double SIZE_VARIANCE = 0.5;
    final static double BASE_DIAMETER = 45;
    private double x;
    private double y;
    private double diameter;
    private double[] rgb;
    private boolean dead;

    final static Random random = new Random();
    AgentEvolution mainProgram;
    VisionOptimiser optimiser;
    VisionOptimiser.Section containingSection;
    GameManager game;
    ArrayList<Spike> otherSpikes;

    public Spike(ArrayList<Spike> otherSpikes, GameManager game, AgentEvolution mainProgram) {
        this.mainProgram = mainProgram;
        this.game = game;
        this.otherSpikes = otherSpikes;
        dead = false;
        optimiser = game.optimiser;

        if (Math.random() > 0.4 && otherSpikes.size() >= 2) {
            //Spawn near last spike
            x = otherSpikes.get(otherSpikes.size() - 1).x + ranRange(60);
            y = otherSpikes.get(otherSpikes.size() - 1).y + ranRange(60);
            //Make sure location is inside the world bounds
            if (x < 0) x += Modes.getWorldWidth();
            else if (x >= Modes.getWorldWidth()) x -= Modes.getWorldWidth();
            if (y < 0) y += Modes.getWorldHeight();
            else if (y >= Modes.getWorldHeight()) y -= Modes.getWorldHeight();
        } else {
            //Random location
            x = Math.random() * Modes.getWorldWidth();
            y = Math.random() * Modes.getWorldHeight();
        }

        //Set diameter
        diameter = BASE_DIAMETER + ranRange(BASE_DIAMETER * SIZE_VARIANCE);
        //Create varied colors
        rgb = new double[3];
        rgb[0] = (((Math.random() * BASE_COLOR_VARIANCE * 2.0) + (1 - BASE_COLOR_VARIANCE)) * BASE_COLOR_RED);
        rgb[1] = (((Math.random() * BASE_COLOR_VARIANCE * 2.0) + (1 - BASE_COLOR_VARIANCE)) * BASE_COLOR_GREEN);
        rgb[2] = (((Math.random() * BASE_COLOR_VARIANCE * 2.0) + (1 - BASE_COLOR_VARIANCE)) * BASE_COLOR_BLUE);

        //Find containing section
        containingSection = optimiser.getSection(x, y);
        containingSection.getSpikes().add(this);
    }

    public void drawSpike() {
        if (!dead && visibleOnScreen() && game.isEnableSpikes()) {
            mainProgram.fill((float) rgb[0], (float) rgb[1], (float) rgb[2]);
            mainProgram.stroke(255, 0, 0);
            mainProgram.strokeWeight(1);
            mainProgram.ellipse((float) x, (float) y, (float) diameter, (float) diameter);
        }
    }

    public void run() {
        if (!dead) {
            //Move around randomly
            if (Math.random() > 0.95) {
                x += random.nextGaussian();
                y += random.nextGaussian();
            }
            if (x > Modes.getWorldWidth()) {
                x -= Modes.getWorldWidth();
            } else if (x < 0) {
                x += Modes.getWorldWidth();
            }
            if (y > Modes.getWorldHeight()) {
                y -= Modes.getWorldHeight();
            } else if (y < 0) {
                y += Modes.getWorldHeight();
            }
        }
    }

    public boolean isDead() {
        return dead;
    }

    private boolean visibleOnScreen() {
        if (game.worldToScreenX(x + diameter) > 0 && game.worldToScreenX(x - diameter) < mainProgram.width) {
            if (game.worldToScreenY(y + diameter) > 0 && game.worldToScreenY(y - diameter) < mainProgram.height) {
                return true;
            }
        }
        return false;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getDiameter() {
        return diameter;
    }

    public double[] getRgb() {
        return rgb;
    }
}
