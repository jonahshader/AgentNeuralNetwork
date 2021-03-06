package GamePieces;

import GamePieces.AgentParts.AgentEye;
import MainParts.GameManager;
import MainParts.GlobalRandom;
import MainParts.Modes;
import NeuralNetStuff.NeuralNetwork;
import VisionOptimisation.Section;
import VisionOptimisation.VisionOptimiser;
import processing.core.PApplet;

import java.io.Serializable;
import java.util.ArrayList;

import static NeuralNetStuff.NeuralNetwork.ranFlip;

/**
 * Created by loshaderj16 on 3/13/2017.
 */

//TODO:
//instead of the agent eating anything touching its body, give it feeders that have an angle and distance that describes where it is in relation to the agent.
//the agent can have as many feeders as it wants, but it takes energy to have them
//genetic mutation can affect the location and count of these feeders

public class Agent implements Serializable {
    private final static double SIZE_SCALE = 1f;
    private final static double CARNIVORE_CONSUME_RATIO = 0.5f; //efficiency, 1 = 100% of energy transferred, the remainder is sent to master energy
    private final static double TURN_FOOD_COST = 0.8; //20
    private final static double IDLE_FOOD_COST = 0.1; // 0.01
    private final static double MOVE_FOOD_COST = 0.1f; // 0.03
    private final static double ENERGY_DIAMETER_SCALE = 0.01f;
    private final static double ENERGY_CONSUMPTION_SIZE_POW = 2; //consumptiom *= diameter^ENERGY_CONSUMPTION_SIZE_POW
    private final static double ENERGY_CONSUMPTION_OVERALL_POW = 1.3f;
    private final static double EAT_AGENT_ENERGY_SCALE = 1.5f; //affects comsumption speed, not efficiency
    private final static double EAT_AGENT_EFFECTIVE_SIZE_SCALE = 0.4f; //check the eatMe method to understand what this means. it essentially means that when eating another agent, this agent's size will be size times this variable. it slows down consumption rate
    private final static double EAT_PLANT_EFFECTIVE_SIZE_SCALE = 2f;

    private final static double MUTATION_RATE_MUTATION_RATE = 0.015; // The mutation rate for the mutation rate (multiplier). Applies to entire neural network

    public final static double MIN_REPRODUCE_ENERGY = 2000;

    //Sensor constants
    private final static double EYE_ANGLE_WIDTH = Math.PI / 4;
    private final static int EYE_COUNT = 3;  //Must be an odd number for now
    public final static double EYE_LENGTH = 350;
    private final static int FEEDBACK_NEURONS = 2;  //The last inputs and output neurons will be linked together. this is the number of neurons that will do this
    //health, energy, diameter, x, y, cos, sin
    private final static int MISC_INPUT_COUNT = 7;
    //red, green, blue, speed, direction change, eat, reproduce
    private final static int MISC_OUTPUT_COUNT = 7;
    //private int totalInputs = 33; //MISC_INPUT_COUNT + (EYE_COUNT * 5)
    private int[] hiddenLayers = new int[]{45, 20, 13};

    //Player control stuff
    private boolean playerControl;
    private boolean spectating = false;

    private ArrayList<Agent> otherAgents;
    private ArrayList<Plant> otherPlants;
    private ArrayList<Spike> otherSpikes;
    private Agent parentAgent;
    private GameManager game;
    //Collision detection/vision stuff
    private VisionOptimiser optimiser;
    private Section containingSection;
    private int sectionX, sectionY;
    private double x;
    private double y;
    private double direction; // in rads
    private double deltaDirection;
    private double pDirection;
    private double speed;
    private double targetSpeed;
    private double energy;
    private double health;
    private double diameter;
    private double mutationRate;
    private double[] rgb;
    private double directionChangeSpeed;
    private boolean eat;
    private boolean reproduce;
    private int age = 0;
    private boolean dead = false;
    private boolean colliding = false;
    private boolean isAStartingAgent;

    //Sensor stuff
    private NeuralNetwork brain;
    private AgentEye[] eyes;

    public Agent(double energy, GameManager game, boolean isPlayer) {
        this.energy = energy;
        this.game = game;
        this.otherAgents = game.getAgents();
        this.otherPlants = game.getPlants();
        this.otherSpikes = game.getSpikes();
        parentAgent = null;
        optimiser = game.optimiser;
        isAStartingAgent = true;
        //Brain inputs:
        /*
        0 to (EYE_COUNT - 1): eye distance
        EYE_COUNT to 2 * EYE_COUNT -1: eye red
        2 * EYE_COUNT to 3 * EYE_COUNT - 1: eye green
        3 * EYE_COUNT to 4 * EYE_COUNT - 1: eye blue
        4 * EYE_COUNT to 5 * EYE_COUNT - 1: eye diameter
         */
        mutationRate = 1;
        brain = new NeuralNetwork((5 * EYE_COUNT) + FEEDBACK_NEURONS + MISC_INPUT_COUNT, MISC_OUTPUT_COUNT + FEEDBACK_NEURONS, hiddenLayers);

        speed = (Math.pow(Math.random(), 2) * 5);
        targetSpeed = speed;
        direction = (Math.random() * Math.PI * 2.0);
        health = 100;

        updateDiameter();
        rgb = new double[3];
        rgb[0] = Math.random() * 255;
        rgb[1] = Math.random() * 255;
        rgb[2] = Math.random() * 255;

        x = (Math.random() * Modes.getWorldWidth());
        y = (Math.random() * Modes.getWorldHeight());

        //Find containing section
        containingSection = optimiser.getSection(x, y);
        containingSection.getAgents().add(this);
        sectionX = optimiser.getSectionID(x);
        sectionY = optimiser.getSectionID(y);

        //Generate some sensor stuff
        eyes = new AgentEye[EYE_COUNT];
        for (int i = 0; i < EYE_COUNT; i++) {
            eyes[i] = new AgentEye(this, (double) (i - (EYE_COUNT / 2)) * EYE_ANGLE_WIDTH, game);
        }

        playerControl = isPlayer;

        updateSensorLocations();
    }

    private Agent(double energy, Agent parentAgent, boolean mutate, double mutationRate, boolean control, boolean spectate) {
        this.mutationRate = mutationRate + GlobalRandom.random.nextGaussian() * MUTATION_RATE_MUTATION_RATE;
        this.energy = energy;
        this.parentAgent = parentAgent;
        this.game = parentAgent.game;
        optimiser = game.optimiser;
        isAStartingAgent = false;

        brain = new NeuralNetwork(parentAgent.brain);
        if (mutate)
            brain.mutateNetWeights(mutationRate);   //Mutate brain

        x = parentAgent.getX() + ranFlip(Math.random() * 20);
        y = parentAgent.getY() + ranFlip(Math.random() * 20);
        direction = Math.random() * Math.PI * 2.0;
        pDirection = direction;
        deltaDirection = 0;
        health = 100;

        speed = parentAgent.speed;
        targetSpeed = parentAgent.targetSpeed;
        rgb = new double[3];
        rgb = parentAgent.rgb.clone();

        //Find containing section
        containingSection = optimiser.getSection(x, y);
        containingSection.getAgents().add(this);
        sectionX = optimiser.getSectionID(x);
        sectionY = optimiser.getSectionID(y);

        //Generate some sensor stuff
        eyes = new AgentEye[EYE_COUNT];
        for (int i = 0; i < EYE_COUNT; i++) {
            eyes[i] = new AgentEye(this, (i - (EYE_COUNT / 2)) * EYE_ANGLE_WIDTH, game);
        }

        playerControl = control;
        spectating = spectate;
        updateSensorLocations();
    }

    public void drawAgent(PApplet graphics) {
        if (visibleOnScreen(graphics) && !dead) {   //Only draw if its alive and on the screen
            if (game.isDrawingEyes())
                for (AgentEye tempEye : eyes) {
                    tempEye.drawEye(graphics);
                }

            graphics.fill((float) rgb[0], (float) rgb[1], (float) rgb[2]);
            double averageColor = (rgb[0] + rgb[1] + rgb[2]) / 3;
            averageColor = (averageColor + 255) / 2.0f;
            graphics.stroke((float) averageColor);
            graphics.ellipse((float) x, (float) y, (float) diameter, (float) diameter);
            graphics.stroke(255);
            graphics.line((float) x, (float) y, (float) (x + (Math.cos(direction) * diameter)), (float) (y + (Math.sin(direction) * diameter)));
            graphics.noStroke();
            double healthScaled = health / 100.0;
            graphics.fill((float) (255 - (healthScaled * 255)), (float) (255 * healthScaled), 0);
            graphics.ellipse((float) x, (float) y, (float) diameter / 3, (float) diameter / 3);
        }
    }

    public void moveAgent(PApplet graphics) {
        checkDead();
        if (!dead) {
            updateDiameter();

            x += Math.cos(direction) * speed;
            y += Math.sin(direction) * speed;
            direction += directionChangeSpeed;

            while (x < 0) x += Modes.getWorldWidth();
            while (x > Modes.getWorldWidth() - 1) x -= Modes.getWorldWidth();
            while (y < 0) y += Modes.getWorldHeight();
            while (y > Modes.getWorldHeight() - 1) y -= Modes.getWorldHeight();

            expendEnergy();
            checkDead();

            if (playerControl || spectating) {
                game.setViewportX(x - (graphics.width / 2f));
                game.setViewportY(y - (graphics.height / 2f));
            }

            updateSensorLocations();
            updateBrain();
        }
    }

    public void runAgent(PApplet graphics) {
        checkContainingSection();
        updateVisibleItems();
        if (!dead) {

            deltaDirection = direction - pDirection;
            deltaDirection = fixAngle(deltaDirection);
            direction = fixAngle(direction);
            pDirection = direction;     //todo: use pDirectionDifference instead of pDirection, then fix angle


            if (playerControl) {
                reproduce = false;
                eat = true;
                direction = Math.atan2(game.screenToWorldY(graphics.mouseY, graphics.height) - y, game.screenToWorldX(graphics.mouseX, graphics.width) - x);
                speed = PApplet.dist((float) x, (float) y, (float) game.screenToWorldX(graphics.mouseX, graphics.width), (float) game.screenToWorldY(graphics.mouseY, graphics.height)) / (graphics.frameRate * 0.1);
            } else {
                controlOutputsWithBrain();
            }

            //Attempt reproduction
            if (reproduce || game.isForceReproduce())
                reproduce();

            //Collision detection is calculated in the loops below
            colliding = false;
            if (eat) {
                //Eat plants
                for (Plant plant : otherPlants) {
                    if (PApplet.dist((float) x, (float) y, (float) plant.getX(), (float) plant.getY()) < (plant.getDiameter() / 2f) + (diameter / 2f)) {
                        energy += plant.eat(Math.sqrt(diameter * EAT_PLANT_EFFECTIVE_SIZE_SCALE / 2.0));
                        colliding = true;
                    }
                }

                //TODO: don't eat children
                //Eat agents
                for (Agent tempAgent : otherAgents) {
                    if (tempAgent != this && !tempAgent.isDead() && tempAgent.getParentAgent() != this) {
                        if (PApplet.dist((float) x, (float) y, (float) tempAgent.getX(), (float) tempAgent.getY()) < (tempAgent.getDiameter() / 2.0) + (diameter / 2.0)) {
                            if (tempAgent != parentAgent) {
                                double foodObtained = tempAgent.eatMe(diameter * EAT_AGENT_ENERGY_SCALE, diameter * EAT_AGENT_EFFECTIVE_SIZE_SCALE);
                                energy += foodObtained * CARNIVORE_CONSUME_RATIO;
                                GameManager.addEnergy(foodObtained * (1.0f - CARNIVORE_CONSUME_RATIO), game);
                                colliding = true;
                            } else { //this is yo parent. dont eat it as fast
                                double foodObtained = tempAgent.eatMe(diameter * EAT_AGENT_ENERGY_SCALE * 0.1, diameter * EAT_AGENT_EFFECTIVE_SIZE_SCALE);
                                energy += foodObtained * CARNIVORE_CONSUME_RATIO;
                                GameManager.addEnergy(foodObtained * (1.0f - CARNIVORE_CONSUME_RATIO), game);
                                colliding = true;
                            }
                        }
                    }
                }
            }
            for (Plant plant : otherPlants) {
                if (PApplet.dist((float) x, (float) y, (float) plant.getX(), (float) plant.getY()) < (plant.getDiameter() / 2.0) + (diameter / 2.0)) {
                    colliding = true;
                    break;
                }
            }
            for (Agent tempAgent : otherAgents) {
                if (tempAgent != this && !tempAgent.isDead()) {
                    if (PApplet.dist((float) x, (float) y, (float) tempAgent.getX(), (float) tempAgent.getY()) < (tempAgent.getDiameter() / 2.0) + (diameter / 2.0)) {
                        colliding = true;
                        break;
                    }
                }
            }
            if (game.isEnableSpikes())
                for (Spike spike : otherSpikes) {
                    if (PApplet.dist((float) x, (float) y, (float) spike.getX(), (float) spike.getY()) < (spike.getDiameter() / 2f) + (diameter / 2f)) {
                        health -= 1;
                        colliding = true;
                    }
                }
            age++;
        }
    }

    private Agent getParentAgent() {
        return parentAgent;
    }

    public void killAgent() {
        dead = true;
        if (energy > 0) {
            GameManager.addEnergy(energy, game);
            System.out.println("Agent killed with excess energy: " + energy);
            energy = 0;
        }
    }

    public double[] getRgb() {
        return rgb;
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

    public double getEnergy() {
        return energy;
    }

    public void enablePlayerControl() {
        playerControl = true;
    }

    public void disablePlayerControl() {
        playerControl = false;
    }

    public void enableSpectating() {
        spectating = true;
    }

    public void disableSpectating() {
        spectating = false;
    }

    public double eatMe(double desiredFood, double enemyDiameter) {
        //This makes it so enemies with a similar diameter will resist being eaten
        double actualFood = desiredFood * (enemyDiameter / diameter);
        //This makes it so smaller agents don't get eaten at a faster rate than desiredFood
        actualFood = Math.min(desiredFood, actualFood);
        if (energy >= actualFood) {
            energy -= actualFood;
            updateDiameter();
            return actualFood;
        } else {
            double temp = energy;
            energy = 0;
            killAgent();
            return temp;
        }
    }

    private double fixAngle(double angle) {
        while (angle < -Math.PI) angle += (Math.PI * 2.0);
        while (angle > Math.PI) angle -= (Math.PI * 2.0);
        return angle;
    }

    private void updateDiameter() {
        diameter = (2.0 * Math.sqrt(energy * SIZE_SCALE / Math.PI));
    }

    private void expendEnergy() {
        reduceEnergy(calculateTotalEnergy());
    }

    public double calculateTotalEnergy() {
        return calculateTotalEnergy(false);
    }

    public double calculateTotalEnergy(boolean print) {
        double energyConsumptionMultiplier = calculateEnergyConsumptionMultiplier();

        double idleEnergy = calculateIdleEnergy();

        double moveEnergy = calculateMoveEnergy() * energyConsumptionMultiplier;
        double turnEnergy = calculateTurnEnergy() * energyConsumptionMultiplier;

        double nonIdle = Math.pow(moveEnergy + turnEnergy, ENERGY_CONSUMPTION_OVERALL_POW);
        double totalEnergy = nonIdle + idleEnergy;

        if (print) {
            System.out.println("Agent idle, move, turn, total energy: " + idleEnergy + " " + moveEnergy + " " + turnEnergy + " " + totalEnergy);
        }

        return totalEnergy;
    }

    public double calculateIdleEnergy() {
        return IDLE_FOOD_COST;
    }

    public double calculateMoveEnergy() {
        return Math.abs(speed) * MOVE_FOOD_COST;
    }

    public double calculateTurnEnergy() {
        return Math.abs(deltaDirection) * TURN_FOOD_COST;
    }

    // multiplier is not applied to idle energy
    public double calculateEnergyConsumptionMultiplier() {
        return Math.pow(diameter * ENERGY_DIAMETER_SCALE, ENERGY_CONSUMPTION_SIZE_POW);
    }

    private void reduceEnergy(double reduction) {
        if (energy - reduction >= 0) {
            energy -= reduction;
            GameManager.addEnergy(reduction, game);
        } else {
            GameManager.addEnergy(energy, game);
            energy = 0;
            killAgent();
        }
    }

    private void checkDead() {
        if (energy <= 0) {
            killAgent();
        }

        if (health <= 0) {
            GameManager.addEnergy(energy, game);
            energy = 0;
            killAgent();
        }
    }

    //Should be called after agent moves
    private void checkContainingSection() {
//        VisionOptimiser.Section tempSection = optimiser.getSection(x, y);
        sectionX = optimiser.getSectionID(x);
        sectionY = optimiser.getSectionID(y);
        if (sectionX != containingSection.getxID() || sectionY != containingSection.getyID()) {
            Section tempSection = optimiser.getSection(x, y);

            //Move this element to a different array in a different section
            tempSection.getAgents().add(this);
            containingSection.getAgents().remove(this);
            containingSection.getVisibleAgents(optimiser).remove(this);
            containingSection = tempSection;
        }
//        if (optimiser.getSection(x, y) != containingSection) {
//            //Move this element to a different array in a different section
//            tempSection.getAgents().add(this);
//            containingSection.getAgents().remove(this);
//            containingSection.getVisibleAgents().remove(this);
//            containingSection = tempSection;
//        }
    }

    private void updateVisibleItems() {
        otherAgents = containingSection.getVisibleAgents(optimiser);
        otherPlants = containingSection.getVisiblePlants(optimiser);
        otherSpikes = containingSection.getVisibleSpikes(optimiser);
    }

    private void updateSensorLocations() {
        for (AgentEye agentEye : eyes) {
            agentEye.updateLocation();
        }
    }

    private void updateBrain() {
        //Update sensors
        for (AgentEye eye : eyes) {
            eye.updateVision(otherPlants, otherAgents, otherSpikes);
        }

        //Send data to brain
        for (int i = 0; i < eyes.length; i++)
            brain.setInput(i, Math.pow(Math.max(((EYE_LENGTH - eyes[i].getItemDistance()) / EYE_LENGTH), 0), 1.0));
        for (int i = eyes.length; i < 2 * eyes.length; i++)
            brain.setInput(i, Math.max(0, Math.min(1, eyes[i - eyes.length].getItemRgbData()[0] / 255.0)) * 5);
        for (int i = 2 * eyes.length; i < 3 * eyes.length; i++)
            brain.setInput(i, Math.max(0, Math.min(1, eyes[i - (2 * eyes.length)].getItemRgbData()[1] / 255.0)) * 5);
        for (int i = 3 * eyes.length; i < 4 * eyes.length; i++)
            brain.setInput(i, Math.max(0, Math.min(1, eyes[i - (3 * eyes.length)].getItemRgbData()[2] / 255.0)) * 5);
        for (int i = 4 * eyes.length; i < 5 * eyes.length; i++)
            brain.setInput(i, (eyes[i - (4 * eyes.length)].getItemDiameter() / 750.0));

        brain.setInput(5 * eyes.length + 0, health / 100);
        brain.setInput(5 * eyes.length + 1, energy / 1200);
        brain.setInput(5 * eyes.length + 2, diameter / 750.0);
        brain.setInput(5 * eyes.length + 3, age / 4000.0);
        brain.setInput(5 * eyes.length + 4, colliding ? 1 : 0);
        brain.setInput(5 * eyes.length + 5, Math.cos(direction));
        brain.setInput(5 * eyes.length + 6, Math.sin(direction));

        //Update brain
        brain.calculateNet();
    }

    private void controlOutputsWithBrain() {
        directionChangeSpeed = brain.getOutput(0) / (Math.PI * 4);
        speed = (brain.getOutput(1) * 3);
        rgb[0] = (brain.getOutput(2)) * 255;
        rgb[1] = ((brain.getOutput(3)) * 255);
        rgb[2] = ((brain.getOutput(4)) * 255);
        eat = brain.getOutput(5) > 0.0;
        reproduce = brain.getOutput(6) > 0.0;

        for (int i = MISC_OUTPUT_COUNT; i < MISC_OUTPUT_COUNT + FEEDBACK_NEURONS; i++) {
            brain.setInput(i - MISC_OUTPUT_COUNT + MISC_INPUT_COUNT + (5 * eyes.length), brain.getOutput(i));
        }
    }

    private boolean visibleOnScreen(PApplet mainProgram) {
        if (game.worldToScreenX(x + diameter, mainProgram) > 0 && game.worldToScreenX(x - diameter, mainProgram) < mainProgram.width) {
            return game.worldToScreenY(y + diameter, mainProgram) > 0 && game.worldToScreenY(y - diameter, mainProgram) < mainProgram.height;
        }
        return false;
    }

    public void reproduce() {
        if (energy >= MIN_REPRODUCE_ENERGY) {
            energy /= 3.0f;
            game.addAgentToAddQueue(new Agent(energy, this, true, mutationRate, false, false));
            game.addAgentToAddQueue(new Agent(energy, this, true, mutationRate, false, false));
        }
    }

    public boolean isDead() {
        return dead;
    }

    public NeuralNetwork getBrain() {
        return brain;
    }

    public double getDirection() {
        return direction;
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public boolean isAStartingAgent() {
        return isAStartingAgent;
    }
}