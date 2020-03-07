package VisionOptimisation;

import GamePieces.Agent;
import GamePieces.Plant;
import GamePieces.Spike;
import processing.core.PApplet;

import java.io.Serializable;
import java.util.ArrayList;

import static VisionOptimisation.VisionOptimiser.SECTION_SIZE;
import static VisionOptimisation.VisionOptimiser.SECTION_VIEW_RANGE;
import static processing.core.PConstants.CORNER;

public class Section implements Serializable {
    private int xID, yID;
    private ArrayList<Agent> agents;        //Agents contained in this section
    private ArrayList<Plant> plants;
    private ArrayList<Spike> spikes;
    private ArrayList<Agent> visibleAgents; //Agents visible (includes agents in other sections)
    private ArrayList<Plant> visiblePlants;
    private ArrayList<Spike> visibleSpikes;

    private boolean visibleAgentsUpdated = false;   //When getVisibleAgents is called, it first checks if this bool is true, if not, it calls calculateAgentVisibilities() to update visibleAgents
    private boolean visiblePlantsUpdated = false;
    private boolean visibleSpikesUpdated = false;

    Section(int xID, int yID) {
        this.xID = xID;
        this.yID = yID;
        this.agents = new ArrayList<>();
        this.plants = new ArrayList<>();
        this.spikes = new ArrayList<>();
        visibleAgents = new ArrayList<>();
        visiblePlants = new ArrayList<>();
        visibleSpikes = new ArrayList<>();
    }

    void drawSectionDebug(PApplet graphics) {
        graphics.rectMode(CORNER);
        graphics.stroke(0, 255, 255);
        graphics.fill(0, 0, 30);
        graphics.rect(xID * SECTION_SIZE, yID * SECTION_SIZE, SECTION_SIZE, SECTION_SIZE);
    }

    public int getxID() {
        return xID;
    }

    public int getyID() {
        return yID;
    }

    void calculateAgentVisibilities(VisionOptimiser visionOptimiser) {
        visibleAgents.clear();
        visiblePlants.clear();
        visibleSpikes.clear();

        for (int xi = (-SECTION_VIEW_RANGE) + xID; xi <= SECTION_VIEW_RANGE + xID; xi++) {
            for (int yi = (-SECTION_VIEW_RANGE) + yID; yi <= SECTION_VIEW_RANGE + yID; yi++) {
                visibleAgents.addAll(visionOptimiser.getSection(xi, yi).getAgents());
                visiblePlants.addAll(visionOptimiser.getSection(xi, yi).getPlants());
                visibleSpikes.addAll(visionOptimiser.getSection(xi, yi).getSpikes());
            }
        }
        visibleAgentsUpdated = true;        //Just updated these visibilities, they are up-to-date
        visiblePlantsUpdated = true;
        visibleSpikesUpdated = true;
    }

    void setVisibilityDataToOld() {
        visibleAgentsUpdated = false;
        visiblePlantsUpdated = false;
        visibleSpikesUpdated = false;
    }

    void removeDeadAgents() {
        agents.removeIf(Agent::isDead);
        visibleAgents.removeIf(Agent::isDead);
        plants.removeIf(Plant::isDead);
        visiblePlants.removeIf(Plant::isDead);
        spikes.removeIf(Spike::isDead);
        visibleSpikes.removeIf(Spike::isDead);
    }

    public ArrayList<Agent> getAgents() {
        return agents;
    }

    public ArrayList<Plant> getPlants() {
        return plants;
    }

    public ArrayList<Spike> getSpikes() {
        return spikes;
    }

    public ArrayList<Agent> getVisibleAgents(VisionOptimiser visionOptimiser) {
        if (!visibleAgentsUpdated) {
            calculateAgentVisibilities(visionOptimiser);
        }
        return visibleAgents;
    }

    public ArrayList<Plant> getVisiblePlants(VisionOptimiser visionOptimiser) {
        if (!visiblePlantsUpdated) {
            calculateAgentVisibilities(visionOptimiser);
        }
        return visiblePlants;
    }

    public ArrayList<Spike> getVisibleSpikes(VisionOptimiser visionOptimiser) {
        if (!visibleSpikesUpdated) {
            calculateAgentVisibilities(visionOptimiser);
        }
        return visibleSpikes;
    }
}