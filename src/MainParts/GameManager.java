package MainParts;

import GamePieces.Agent;
import GamePieces.Plant;
import GamePieces.Spike;
import VisionOptimisation.VisionOptimiser;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.event.MouseEvent;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Created by Jonah on 3/26/2017.
 */
public class GameManager implements Serializable {
    private int epoch = 0;
    private long time = 0;
    private double targetFps = 144;

    private ArrayList<Agent> agents;
    private ArrayList<Agent> agentsToAdd;
    private ArrayList<Plant> plants;
    private ArrayList<Plant> plantsToAdd;
    private ArrayList<Spike> spikes;
    private ArrayList<Spike> spikesToAdd;

    public VisionOptimiser optimiser;
    private GameHUD hud;

    private boolean drawingEnabled = true;
    private boolean drawingPlants = true;
    private boolean drawingBrain = false;
    private boolean drawingEyes = true;
    private boolean drawingInitialAgents = true;

    private boolean drawingDebugGraphics = false;
    private boolean forceReproduce = false;
    private boolean enableSpikes = true;
    private double viewportX = 0;
    private double viewportY = 0;
    private double viewportZoom = 1;

    //System wide energy
    private double unusedEnergy;

    private NumberFormat formatter;

    GameManager(PApplet mainClass) {
        Modes.setScale(1.0f); //0.6
        //uncomment for carnivore mode
        Modes.setDifficultyMode(Modes.Mode.NO_PLANTS);
//        Modes.disableSpikes();

        //Determine initial energy the simulation will have based on the number of agents, agent starting energy, and a multiplier (energy boost scale)
        unusedEnergy = (Modes.getStartingAgentCount() * Modes.getMinimumStartingAgentEnergy()) * Modes.getStartingEnergyBoostScale();

        //Init formatter (for numerical text formatting).
        formatter = new DecimalFormat("#0.00");
        //Init HUD for displaying some data
        hud = new GameHUD(this);
        //Create the environment (controls day/night cycle)
        //Create the optimiser object
        optimiser = new VisionOptimiser(Modes.getWorldWidth(), Modes.getWorldHeight());
        //init arraylists
        agents = new ArrayList<>();
        agentsToAdd = new ArrayList<>();
        plants = new ArrayList<>();
        plantsToAdd = new ArrayList<>();
        spikes = new ArrayList<>();
        spikesToAdd = new ArrayList<>();

        //spawn initial agents
        while (agents.size() < Modes.getStartingAgentCount()) {
            agents.add(new Agent(Modes.getMinimumStartingAgentEnergy(), this, false));
            unusedEnergy -= Modes.getMinimumStartingAgentEnergy();
        }

        //spawn initial spikes
        for (int i = 0; i < Modes.getStartingSpikeCount(); i++) {
            spikes.add(new Spike(spikes, this));
        }

        //spawn initial plants
        for (int i = 0; i < Modes.getStartingPlantCount(); i++) {
            plants.add(new Plant(plants, this));
        }

        mainClass.frameRate((float) targetFps);
    }

    void draw(PApplet mainClass) {
        //When drawingEnabled is enabled, render and run the simulation
        if(drawingEnabled) {
            run(mainClass);
            render(mainClass);
        } else {
            //If we are not drawing to the screen, run the simulation multiple times per frame
            //I do this because rendering a blank frame still takes resources, so now I am only
            //rendering a blank frame every 100th tick of the simulation, saving resources
            for (int i = 0; i < 100; i++) {
                run(mainClass);
            }
        }
    }

    private void render(PApplet mainClass) {
        if (mainClass.mouseButton == PConstants.CENTER && mainClass.mousePressed) {
            viewportX += (mainClass.pmouseX - mainClass.mouseX) / viewportZoom;
            viewportY += (mainClass.pmouseY - mainClass.mouseY) / viewportZoom;
        }

        //Apply viewport translation
        mainClass.pushMatrix();
        mainClass.translate(mainClass.width / 2f, mainClass.height / 2f);
        mainClass.scale((float) viewportZoom);
        mainClass.translate( -mainClass.width / 2f, -mainClass.height / 2f);
        mainClass.translate((float) -viewportX, (float) -viewportY);
        mainClass.background(0);
        if (drawingDebugGraphics)
            optimiser.drawDebug(mainClass);

        if (drawingPlants) {
            for (Plant plant : plants)
                plant.drawPlant(mainClass);
        }

        for (Spike spike : spikes)
            spike.drawSpike(mainClass);

        if (drawingInitialAgents) {
            for (Agent agent : agents)
                agent.drawAgent(mainClass);
        } else {
            for (Agent agent : agents)
                if (!agent.isAStartingAgent())
                    agent.drawAgent(mainClass);
        }

        mainClass.popMatrix();

        hud.drawHUD(mainClass);
    }

    void mousePressed(PApplet mainClass) {
        if (drawingEnabled) {                             //Only handle these mouse presses when you can see what you are clicking!
            hud.mousePressed();
            if (mainClass.mouseButton == PConstants.LEFT) {
                for (Agent agent : agents) {
                    if (PApplet.dist((float) agent.getX(), (float) agent.getY(), (float) screenToWorldX(mainClass), (float) screenToWorldY(mainClass)) < agent.getDiameter() / 2.0) {
                        agent.enablePlayerControl();
                    } else {
                        agent.disablePlayerControl();
                    }
                }
            } else if (mainClass.mouseButton == PConstants.RIGHT) {
                for (Agent agent : agents) {
                    if (PApplet.dist((float) agent.getX(), (float) agent.getY(), (float) screenToWorldX(mainClass), (float) screenToWorldY(mainClass)) < agent.getDiameter() / 2.0) {
                        agent.killAgent();
                    } else {
                        agent.disablePlayerControl();
                    }
                }
            } else {
                hud.setSpectatingAgent(null);
                for (Agent agent : agents) {
                    if (PApplet.dist((float) agent.getX(), (float) agent.getY(), (float) screenToWorldX(mainClass), (float) screenToWorldY(mainClass)) < agent.getDiameter() / 2.0) {
                        agent.enableSpectating();
                        hud.setSpectatingAgent(agent);
                    } else {
                        agent.disableSpectating();
                    }
                }
            }
        }
    }

    void keyPressed(PApplet mainClass) {
        if (mainClass.key == 'o' || mainClass.key == 'O') drawingEnabled = !drawingEnabled;
        if (drawingEnabled) {
            mainClass.frameRate((float) targetFps);
        } else {
            mainClass.frameRate(9999999);
        }

        if (mainClass.key == 'p' || mainClass.key == 'P') drawingPlants = !drawingPlants;
        if (mainClass.key == 'g' || mainClass.key == 'G') drawingDebugGraphics = !drawingDebugGraphics;
        if (mainClass.key == '+') targetFps *= 1.5;
        if (mainClass.key == '-') targetFps /= 1.5;
        if (mainClass.key == 'q' || mainClass.key == 'Q') viewportZoom /= 1.5;
        if (mainClass.key == 'e' || mainClass.key == 'E') viewportZoom *= 1.5;
        if (mainClass.key == 'b' || mainClass.key == 'B') drawingBrain = !drawingBrain;
        if (mainClass.key == 'h' || mainClass.key == 'H') drawingEyes = !drawingEyes;
        if (mainClass.key == 'f' || mainClass.key == 'F') forceReproduce = !forceReproduce;
        if (mainClass.key == 'l' || mainClass.key == 'L') enableSpikes = !enableSpikes;
        if (mainClass.key == 'x' || mainClass.key == 'X') drawingInitialAgents = !drawingInitialAgents;

        if (mainClass.key == ' ') {
            hud.setSpectatingAgent(null);
            for (Agent agent : agents) {
                if (PApplet.dist((float) agent.getX(), (float) agent.getY(), (float) screenToWorldX(mainClass), (float) screenToWorldY(mainClass)) < agent.getDiameter() / 2.0) {
                    agent.enableSpectating();
                    hud.setSpectatingAgent(agent);
                } else {
                    agent.disableSpectating();
                }
            }
        }
    }

    public static synchronized void addEnergy(double addedEnergy, GameManager gmInstance) {
        gmInstance.unusedEnergy += addedEnergy;
    }
    // takes and returns the desired food amount from the system.
    // if there is not enough food, it will return as much as possible until it hits 0.
    public double takeEnergy(double desiredEnergy) {
        if (unusedEnergy - desiredEnergy > 0) {
            unusedEnergy -= desiredEnergy;
            return desiredEnergy;
        } else {
            double temp = unusedEnergy;
            unusedEnergy = 0;
            return temp;
        }
    }

    public ArrayList<Agent> getAgents() {
        return agents;
    }

    public void addAgentToAddQueue(Agent agent) {
        agentsToAdd.add(agent);
    }

    public void addPlantToAddQueue(Plant plant) {
        plantsToAdd.add(plant);
    }

    public ArrayList<Plant> getPlants() {
        return plants;
    }

    public double screenToWorldX(PApplet mainClass) {
        return ((((mainClass.mouseX + (-mainClass.width / 2.0)) / viewportZoom) + (mainClass.width / 2.0)) + viewportX);
    }

    public double screenToWorldX(int mouseX, int width) {
        return ((((mouseX + (-width / 2.0)) / viewportZoom) + (width / 2.0)) + viewportX);
    }

    public double screenToWorldY(PApplet mainClass) {
        return ((((mainClass.mouseY + (-mainClass.height / 2.0)) / viewportZoom) + (mainClass.height / 2.0)) + viewportY);
    }

    public double screenToWorldY(int mouseY, int height) {
        return ((((mouseY + (-height / 2.0)) / viewportZoom) + (height / 2.0)) + viewportY);
    }

    public double worldToScreenX(double xIn, PApplet mainClass) {
        return ((((xIn - viewportX) - (mainClass.width / 2.0)) * viewportZoom) + (mainClass.width / 2.0));
    }

    public double worldToScreenY(double yIn, PApplet mainClass) {
        return (((yIn - viewportY - (mainClass.height / 2.0)) * viewportZoom) + (mainClass.height / 2.0));
    }

    public void setViewportX(double viewportX) {
        this.viewportX = viewportX;
    }

    public void setViewportY(double viewportY) {
        this.viewportY = viewportY;
    }

    public void setViewportZoom(double viewportZoom) {
        this.viewportZoom = viewportZoom;
    }

    void mouseWheel(MouseEvent event) {
        double e = event.getCount();
        viewportZoom *= Math.pow(0.8, e);
    }

    public boolean isDrawingDebugGraphics() {
        return drawingDebugGraphics;
    }

    double getUnusedEnergy() {
        return unusedEnergy;
    }

    boolean isDrawingBrain() {
        return drawingBrain;
    }

    public boolean isDrawingEyes() {
        return drawingEyes;
    }

    public boolean isForceReproduce() {
        return forceReproduce;
    }

    public boolean isEnableSpikes() {
        return enableSpikes;
    }

    public void setEnableSpikes(boolean enableSpikes) {
        this.enableSpikes = enableSpikes;
    }

    //Main computation
    private void run(PApplet mainClass) {
        if (drawingEnabled) {
            if (mainClass.frameCount % (30) == 0) {
                mainClass.getSurface().setTitle("Epoch: " + epoch + " FPS: " + formatter.format(mainClass.frameRate));
            }
        } else {
            mainClass.getSurface().setTitle("Epoch: " + epoch + " FPS: " + formatter.format(mainClass.frameRate * 100));
        }

        //Keyboard viewport control
        if (mainClass.keyPressed) {
            if (mainClass.key == 'a' || mainClass.key == 'A') {
                viewportX -= 5 / viewportZoom;
            }
            if (mainClass.key == 'd' || mainClass.key == 'D') {
                viewportX += 5 / viewportZoom;
            }
            if (mainClass.key == 's' || mainClass.key == 'S') {
                viewportY += 5 / viewportZoom;
            }
            if (mainClass.key == 'w' || mainClass.key == 'W') {
                viewportY -= 5 / viewportZoom;
            }
        }

        if (time % 20 == 0)
            optimiser.runOptimiser();

        //if there are too few agents in the simulation, spawn more
        if (agents.size() < Modes.getMinimumAgentCount()) {
            agents.add(new Agent(Modes.getMinimumStartingAgentEnergy(), this, false));
//            System.out.println("Population too low; adding new agent");
            unusedEnergy -= Modes.getMinimumStartingAgentEnergy();
//            System.out.println("below threshold. agent added");
        } else if (time % 72000 == 0){ //also spawn one periodically to add genetic variance to the simulation
            agents.add(new Agent(Modes.getMinimumStartingAgentEnergy(), this, false));
            unusedEnergy -= Modes.getMinimumStartingAgentEnergy();
//            System.out.println("periodic agent added");
        }

        //increment epoch
        if (time % 20000 == 19999) {
            epoch++;
            double averageMutationRate = 0;
            for (Agent agent : agents) {
                averageMutationRate += agent.getMutationRate();
            }
            averageMutationRate /= agents.size();
//            System.out.println("Average mutation rate at epoch " + epoch + " is " + averageMutationRate);
            System.out.println(epoch + "," + averageMutationRate);
        }

//        plants.parallelStream().forEach(Plant::grow);
        for (Plant plant : plants) {
            plant.grow();
        }

        for (Spike spike : spikes) {
            spike.run();
        }

        ArrayList<Plant> plantsToRemove = new ArrayList<>();
        for (Plant plant : plants) {
            if (plant.isDead()) {
                plantsToRemove.add(plant);
            }
        }
        plants.removeAll(plantsToRemove);

//        agents.parallelStream().forEach(Agent::runAgent);
        for (Agent agent : agents) agent.runAgent(mainClass);
        agents.parallelStream().forEach(agent -> agent.moveAgent(mainClass));
//        agents.forEach(Agent::moveAgent);
//        for (Agent agent : agents) {
//            agent.moveAgent(mainClass);
//        }

        ArrayList<Agent> toRemove = new ArrayList<>();
        for (Agent agent : agents) {
            if (agent.isDead()) {
                toRemove.add(agent);
            }
        }
        agents.removeAll(toRemove);

        ArrayList<Spike> spikesToRemove = new ArrayList<>();
        for (Spike spike : spikes) {
            if (spike.isDead()) {
                spikesToRemove.add(spike);
            }
        }
        spikes.removeAll(spikesToRemove);

        //Add plants that were created before
        if (plantsToAdd.size() > 0) {
            plants.addAll(plantsToAdd);
            plantsToAdd.clear();
        }
        //Add agents that were created before
        if (agentsToAdd.size() > 0) {
            agents.addAll(agentsToAdd);
            agentsToAdd.clear();
        }
        //Add spikes that were created before
        if (spikesToAdd.size() > 0) {
            spikes.addAll(spikesToAdd);
            spikesToAdd.clear();
        }

        time++;
    }

    // returns total energy in the system
    double calculateTotalEnergy() {
        double totalEnergy = unusedEnergy;
        for (Agent agent : agents) {
            totalEnergy += agent.getEnergy();
        }
        for (Plant plant : plants) {
            totalEnergy += plant.getFood();
        }

        return totalEnergy;
    }

    public ArrayList<Spike> getSpikes() {
        return spikes;
    }
}
