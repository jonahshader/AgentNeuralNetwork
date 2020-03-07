package GamePieces.AgentParts;

import GamePieces.Agent;
import GamePieces.Plant;
import GamePieces.Spike;
import MainParts.GameManager;
import processing.core.PApplet;

import java.awt.geom.Line2D;
import java.io.Serializable;
import java.util.ArrayList;

import static GamePieces.Agent.EYE_LENGTH;

/**
 * Created by Jonah on 3/28/2017.
 */
public class AgentEye implements Serializable {
    private Agent parentAgent;
    private Line2D eyeLine;
    private GameManager game;
    private double relativeAngle;    //Angle difference from center line

    //Outputs
    private double[] itemRgbData;  //red, green, blue in the range 0 - 255
    private double itemDistance;
    private double itemDiameter;

    public AgentEye(Agent parentAgent, double relativeAngle, GameManager game) {
        this.parentAgent = parentAgent;
        this.relativeAngle = relativeAngle;
        this.game = game;
        itemRgbData = new double[3];
        eyeLine = new Line2D.Float();
    }

    public void updateVision(ArrayList<Plant> visiblePlants, ArrayList<Agent> visibleAgents, ArrayList<Spike> visibleSpikes) {
        float shortestDistance = 999999999; //Float.MAX_VALUE might use more cpu for some reason
        Agent closestAgent = null;
        Plant closestPlant = null;
        Spike closestSpike = null;

        for (Agent agent : visibleAgents) {
            if (agent != parentAgent) {
                if (eyeLine.ptSegDist(agent.getX(), agent.getY()) < agent.getDiameter() / 2) {  //Circle collided, it is visible to this eye
                    float tempDistance = PApplet.dist((float) parentAgent.getX(), (float) parentAgent.getY(), (float) agent.getX(), (float) agent.getY());

                    if (tempDistance < shortestDistance) {
                        closestAgent = agent;
                        shortestDistance = tempDistance;
                    }
                }
            }
        }

        for (Plant plant : visiblePlants) {
            if (eyeLine.ptSegDist(plant.getX(), plant.getY()) < plant.getDiameter() / 2) {  //Circle collided, it is visible to this eye
                float tempDistance = PApplet.dist((float) parentAgent.getX(), (float) parentAgent.getY(), (float) plant.getX(), (float) plant.getY());

                if (tempDistance < shortestDistance) {
                    closestPlant = plant;
                    closestAgent = null;    //Agent is no longer the closest object to the eye, make it null so it can't be used
                    shortestDistance = tempDistance;
                }
            }
        }

        if (game.isEnableSpikes())
            for (Spike spike : visibleSpikes) {
                if (eyeLine.ptSegDist(spike.getX(), spike.getY()) < spike.getDiameter() / 2) {  //Circle collided, it is visible to this eye
                    float tempDistance = PApplet.dist((float) parentAgent.getX(), (float) parentAgent.getY(), (float) spike.getX(), (float) spike.getY());

                    if (tempDistance < shortestDistance) {
                        closestSpike = spike;
                        closestAgent = null; //Agent is no longer the closest object to the eye, make it null so it can't be used
                        closestPlant = null; //Plant is no longer the closest object to the eye, make it null so it can't be used
                        shortestDistance = tempDistance;
                    }
                }
            }

        itemDistance = shortestDistance;//This bit of data can be obtained with both agents and plants, it doesn't need to be placed in one of the if statement bodies below
        if (closestAgent != null) {     //An agent is the closest to the eye, get data from it
            itemDiameter = closestAgent.getDiameter();
            itemRgbData = closestAgent.getRgb().clone();
        } else if (closestPlant != null) {                        //A plant is the closest to the eye, get data from it
            itemDiameter = closestPlant.getDiameter();
            itemRgbData = closestPlant.getRgb().clone();
        } else if (closestSpike != null) {                                            //It didn't see anything
            itemDiameter = closestSpike.getDiameter();
            itemRgbData = closestSpike.getRgb().clone();
        } else {
            itemDiameter = 0;
            itemRgbData[0] = 0;
            itemRgbData[1] = 0;
            itemRgbData[2] = 0;
        }
    }

    public void drawEye(PApplet graphics) {
        graphics.stroke((float) itemRgbData[0], (float) itemRgbData[1], (float) itemRgbData[2]);
        graphics.line((float) eyeLine.getX1(), (float) eyeLine.getY1(), (float) eyeLine.getX2(), (float) eyeLine.getY2());
    }

    public double[] getItemRgbData() {
        return itemRgbData;
    }

    public double getItemDistance() {
        return Math.max(0, itemDistance);
    }

    public double getItemDiameter() {
//        System.out.println("diameter: " + itemDiameter);
        return itemDiameter;
    }

    public void updateLocation() {
        eyeLine.setLine(parentAgent.getX(), parentAgent.getY(), (parentAgent.getX() + (Math.cos(parentAgent.getDirection() + relativeAngle) * EYE_LENGTH)), (parentAgent.getY() + (Math.sin(parentAgent.getDirection() + relativeAngle) * EYE_LENGTH)));
    }
}
