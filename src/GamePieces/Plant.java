package GamePieces;

import MainParts.AgentEvolution;
import MainParts.GameManager;
import MainParts.Modes;
import VisionOptimisation.Section;
import VisionOptimisation.VisionOptimiser;
import processing.core.PApplet;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by loshaderj16 on 3/13/2017.
 */

//TODO: make the plants produce offsprings with mutated colors. brighter colors grow faster. 255, 255, 255 would grow at 100%. 0, 0, 0 would grow at 0% (and should be killed immediately)
//This is just food for the agents, nothing else
public class Plant implements Serializable {
    final static int BASE_COLOR_RED = 40;
    final static int BASE_COLOR_GREEN = 220;
    final static int BASE_COLOR_BLUE = 25;
    final static double BASE_COLOR_VARIANCE = 0.1f; //Percentage
    final static double MAX_GROW_RATE = 1.1;
    final static double SIZE_SCALE = 3;
    VisionOptimiser optimiser;
    Section containingSection;
    GameManager game;
    ArrayList<Plant> otherPlants;
    boolean dead = false;       //If dead, this was already killed and the energy was managed, it is safe to remove this from any and all arraylists
    private double x;
    private double y;
    private double food;
    private double growRate;
    private double diameter;
    private double[] rgb;

    public Plant(ArrayList<Plant> otherPlants, GameManager game) {
        this.otherPlants = otherPlants;
        this.game = game;
        optimiser = game.optimiser;

        //Random location in world
        x = Math.random() * Modes.getWorldWidth();
        y = Math.random() * Modes.getWorldHeight();

        ArrayList<Spike> spikes = game.getSpikes();
        for (int i = 0; i < spikes.size(); i++) {
            Spike spike = spikes.get(i);
            double distToSpike = PApplet.dist((float) spike.getX(), (float) spike.getY(), (float) x, (float) y);
            if (distToSpike - spike.getDiameter() < 100) {
                //Random location in world
                x = Math.random() * Modes.getWorldWidth();
                y = Math.random() * Modes.getWorldHeight();
                i = 0;
            }
        }

        //Grow rate is anywhere between 0 and MAX_GROW_RATE
        growRate = Math.random() * MAX_GROW_RATE;

        //Plants start at food 0 and grow to their asymptote
        food = 0;

        //Update diameter (instead of instantiating diameter)
        updateDiameter();

        //Create varied colors
        rgb = new double[3];
        rgb[0] = (((Math.random() * BASE_COLOR_VARIANCE * 2.0) + (1 - BASE_COLOR_VARIANCE)) * BASE_COLOR_RED);
        rgb[1] = (((Math.random() * BASE_COLOR_VARIANCE * 2.0) + (1 - BASE_COLOR_VARIANCE)) * BASE_COLOR_GREEN);
        rgb[2] = (((Math.random() * BASE_COLOR_VARIANCE * 2.0) + (1 - BASE_COLOR_VARIANCE)) * BASE_COLOR_BLUE);


        //Find containing section
        containingSection = optimiser.getSection(x, y);
        containingSection.getPlants().add(this);
    }

    public void drawPlant(PApplet mainProgram) {
        if (!dead && visibleOnScreen(mainProgram)) {
            mainProgram.fill((float) rgb[0], (float) rgb[1], (float) rgb[2]);
            mainProgram.noStroke();
            mainProgram.ellipse((float) x, (float) y, (float) diameter, (float) diameter);
        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getFood() {
        return food;
    }

    public double getDiameter() {
        return diameter;
    }

    public double eat(double desiredFood) { //Allows agent to eatMe desiredFood. returns how much food it obtained. TODO: make plant less efficient for drawing higher amounts of food
        if (!dead) {
            if (food >= desiredFood) {
                food -= desiredFood;
                updateDiameter();
                return desiredFood;
            } else {
                game.addPlantToAddQueue(new Plant(otherPlants, game));
                dead = true;
                double tempFood = food;
                food = 0;
                updateDiameter();
                return tempFood;
            }
        } else {
            return 0;
        }
    }

    public void grow() {
        if (!dead) {
            double newFood = food;
            newFood += growRate;
            newFood *= 0.9993;
//            if (game.getAgents().size() < Modes.getMinimumAgentCount()) {
//                 desiredFood = 0;
//            }

            double foodGain = game.takeEnergy(newFood - food);
            food += foodGain;
            updateDiameter();
        }
    }

    public double[] getRgb() {
        return rgb;
    }

    private void updateDiameter() {
        diameter = (2.0 * Math.sqrt(food * SIZE_SCALE / Math.PI));
    }

    private boolean visibleOnScreen(PApplet mainProgram) {
        if (game.worldToScreenX(x + diameter, mainProgram) > 0 && game.worldToScreenX(x - diameter, mainProgram) < mainProgram.width) {
            if (game.worldToScreenY(y + diameter, mainProgram) > 0 && game.worldToScreenY(y - diameter, mainProgram) < mainProgram.height) {
                return true;
            }
        }
        return false;
    }

    public boolean isDead() {
        return dead;
    }
}
