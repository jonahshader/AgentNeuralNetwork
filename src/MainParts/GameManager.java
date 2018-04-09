package MainParts;

import GamePieces.Agent;
import GamePieces.Plant;
import GamePieces.Spike;
import VisionOptimisation.VisionOptimiser;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.event.MouseEvent;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Created by Jonah on 3/26/2017.
 */
public class GameManager {
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
    public static Environment environment;
    private GameHUD hud;
    private AgentEvolution mainClass;

    private boolean drawing = true;
    private boolean drawingPlants = true;
    private boolean drawingBrain = false;
    private boolean drawingEyes = true;

    private boolean drawingDebugGraphics = false;
    private boolean forceReproduce = false;
    private boolean enableSpikes = true;
    private double viewportX = 0;
    private double viewportY = 0;
    private double viewportZoom = 1;
    private boolean playingGame;

    //System wide energy
    private double unusedEnergy;

    NumberFormat formatter;

    public ArrayList<Spike> getSpikes() {
        return spikes;
    }

    public GameManager(AgentEvolution mainClass) {
        playingGame = false;
        Modes.setScale(0.1f); //0.6
        unusedEnergy = (Modes.getStartingAgentCount() * Modes.getMinimumStartingAgentEnergy()) * Modes.getStartingEnergyBoostScale();

        formatter = new DecimalFormat("#0.00");
        hud = new GameHUD(mainClass, this);
        environment = new Environment();
        environment.calculateEnvironment(mainClass.frameCount);
        optimiser = new VisionOptimiser(Modes.getWorldWidth(), Modes.getWorldHeight(), mainClass);
        this.mainClass = mainClass;
        agents = new ArrayList<>();
        agentsToAdd = new ArrayList<>();
        plants = new ArrayList<>();
        plantsToAdd = new ArrayList<>();
        spikes = new ArrayList<>();
        spikesToAdd = new ArrayList<>();


        while (unusedEnergy >= Modes.getMinimumStartingAgentEnergy() + (Modes.getMinimumAgentCount() * Modes.getMinimumStartingAgentEnergy())) {
            if (agents.size() >= Modes.getStartingAgentCount())
                break;
            agents.add(new Agent(Modes.getMinimumStartingAgentEnergy(), this, mainClass, false));
            unusedEnergy -= Modes.getMinimumStartingAgentEnergy();
        }

        for (int i = 0; i < Modes.getStartingSpikeCount(); i++) {
            spikes.add(new Spike(spikes, this, mainClass));
        }

        for (int i = 0; i < Modes.getStartingPlantCount(); i++) {
            plants.add(new Plant(plants, this, mainClass));
        }

        mainClass.frameRate((float) targetFps);
    }

    public void draw() {
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

        if (mainClass.frameCount % 1000 == 0)
            optimiser.runOptimiser();

        if (agents.size() < Modes.getMinimumAgentCount()) {
            if (unusedEnergy >= Modes.getMinimumStartingAgentEnergy()) {
                agents.add(new Agent(Modes.getMinimumStartingAgentEnergy(), this, mainClass, false));
                unusedEnergy -= Modes.getMinimumStartingAgentEnergy();
            }
        } else if (mainClass.frameCount % 720 == 0){
            agents.add(new Agent(Modes.getMinimumStartingAgentEnergy(), this, mainClass, false));
            unusedEnergy -= Modes.getMinimumStartingAgentEnergy();
        }


        //When drawing is enabled, draw stuff :)
        if(drawing) {
            if (mainClass.mouseButton == PConstants.CENTER && mainClass.mousePressed) {
                viewportX += (mainClass.pmouseX - mainClass.mouseX) / viewportZoom;
                viewportY += (mainClass.pmouseY - mainClass.mouseY) / viewportZoom;
            }

            //Apply viewport translation
            mainClass.pushMatrix();
            mainClass.translate(mainClass.width / 2, mainClass.height / 2);
            mainClass.scale((float) viewportZoom);
            mainClass.translate( -mainClass.width / 2, -mainClass.height / 2);
            mainClass.translate((float) -viewportX, (float) -viewportY);
            mainClass.background(environment.getBrightness());
            if (drawingDebugGraphics)
                optimiser.drawDebug();
            if (drawingPlants)
                for (int i = 0; i < plants.size(); i++) {
                    Plant plant = plants.get(i);
                    plant.drawPlant();
                }
            for (int i = 0; i < spikes.size(); i++) {
                Spike spike = spikes.get(i);
                spike.drawSpike();
            }
            for (int i = 0; i < agents.size(); i++) {
                Agent agent = agents.get(i);
                agent.drawAgent();
            }
            mainClass.popMatrix();

            hud.drawHUD();
        }

        //Main computation
        if (drawing) {
            run();
        } else {
            for (int i = 0; i < 100; i++) {
                run();
            }
        }


        if (drawing) {
            if (mainClass.frameCount % (30) == 0) {
                mainClass.getSurface().setTitle("Epoch: " + epoch + " FPS: " + formatter.format(mainClass.frameRate));
//                System.out.println("FPS: " + mainClass.frameRate);
            }
        } else {
            if (mainClass.frameCount % 200 == 0) {
                mainClass.getSurface().setTitle("Epoch: " + epoch + " FPS: " + formatter.format(mainClass.frameRate * 100));
//                System.out.println("FPS: " + mainClass.frameRate);
            }
        }
    }

    public boolean isPlayingGame() {
        return playingGame;
    }

    public void mousePressed() {
        if (drawing) {                             //Only handle these mouse presses when you can see what you are clicking!
            hud.mousePressed();
            if (mainClass.mouseButton == PConstants.LEFT) {
                for (Agent agent : agents) {
                    if (PApplet.dist((float) agent.getX(), (float) agent.getY(), (float) screenToWorldX(), (float) screenToWorldY()) < agent.getDiameter() / 2.0) {
                        Agent controlledAgent = agent;
                        controlledAgent.enablePlayerControl();
                    } else {
                        agent.disablePlayerControl();
                    }
                }
            } else if (mainClass.mouseButton == PConstants.RIGHT) {
                for (int i = 0; i < agents.size(); i++) {
                    Agent agent = agents.get(i);

                    if (PApplet.dist((float) agent.getX(), (float) agent.getY(), (float) screenToWorldX(), (float) screenToWorldY()) < agent.getDiameter() / 2.0) {
                        agent.killAgent();
                    } else {
                        agent.disablePlayerControl();
                    }
                }
            } else {
                hud.setSpectatingAgent(null);
                for (Agent agent : agents) {
                    if (PApplet.dist((float) agent.getX(), (float) agent.getY(), (float) screenToWorldX(), (float) screenToWorldY()) < agent.getDiameter() / 2.0) {
                        agent.enableSpectating();
                        hud.setSpectatingAgent(agent);
                    } else {
                        agent.disableSpectating();
                    }
                }
            }
        }
    }

    public void keyPressed() {
        if (mainClass.key == 'o' || mainClass.key == 'O') drawing = !drawing;
        if (drawing) {
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

        if (mainClass.key == ' ') {
            hud.setSpectatingAgent(null);
            for (Agent agent : agents) {
                if (PApplet.dist((float) agent.getX(), (float) agent.getY(), (float) screenToWorldX(), (float) screenToWorldY()) < agent.getDiameter() / 2.0) {
                    agent.enableSpectating();
                    hud.setSpectatingAgent(agent);
                } else {
                    agent.disableSpectating();
                }
            }
        }
    }

    public void addEnergy(double addedEnergy) {
        unusedEnergy += addedEnergy;
    }

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

    public double screenToWorldX() {
        return ((((mainClass.mouseX + (-mainClass.width / 2.0)) / viewportZoom) + (mainClass.width / 2.0)) + viewportX);
    }

    public double screenToWorldY() {
        return ((((mainClass.mouseY + (-mainClass.height / 2.0)) / viewportZoom) + (mainClass.height / 2.0)) + viewportY);
    }

    public double worldToScreenX(double xIn) {
        return ((((xIn - viewportX) - (mainClass.width / 2.0)) * viewportZoom) + (mainClass.width / 2.0));
    }

    public double worldToScreenY(double yIn) {
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

    public void mouseWheel(MouseEvent event) {
        double e = event.getCount();
        viewportZoom *= Math.pow(0.8, e);
    }

    public boolean isDrawingDebugGraphics() {
        return drawingDebugGraphics;
    }

    public double getUnusedEnergy() {
        return unusedEnergy;
    }

    public boolean isDrawingBrain() {
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

    public void startGame() {
    }

    //Main computation
    private void run() {
        if (time % 20000 == 19999) epoch++;

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

        for (Agent agent : agents) {
            agent.runAgent();
        }

        for (Agent agent : agents) {
            agent.moveAgent();
        }

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

        environment.calculateEnvironment(mainClass.frameCount);
        time++;
    }
}
