package VisionOptimisation;

import GamePieces.Agent;
import GamePieces.Plant;
import GamePieces.Spike;
import processing.core.PApplet;

import java.util.ArrayList;
import java.util.Iterator;

import static processing.core.PConstants.CORNER;

/**
 * Created by Jonah on 3/26/2017.
 */
//TODO: rewrite entire vision optimiser so that the agents and plants decide what section they are in.
public class VisionOptimiser {
    final static int SECTION_SIZE = 350;///256
    final static int SECTION_VIEW_RANGE = 1; // additional sections in all directions the content can react to. should be at least 1
    int worldWidth;
    int worldHeight;

    PApplet graphics;   //For drawing debug lines
    int time = 0;

    Section[][] sections;
    int sectionWidthCount;
    int sectionHeightCount;

    public VisionOptimiser(int worldWidth, int worldHeight, PApplet graphics) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.graphics = graphics;

        //TODO: might need to use floor() instead of casting to (int) here
        sectionWidthCount = (int) ((((float) worldWidth) / SECTION_SIZE) + 1);
        sectionHeightCount = (int) ((((float) worldHeight) / SECTION_SIZE) + 1);

        sections = new Section[sectionWidthCount][sectionHeightCount];
        for (int x = 0; x < sectionWidthCount; x++) {
            for (int y = 0; y < sectionHeightCount; y++) {
                sections[x][y] = new Section(x, y);
            }
        }
    }

    //Must be called before all processing takes place involving GamePieces
    public void runOptimiser() {
        for (Section[] sectionSlice : sections) {
            for (Section section : sectionSlice) {
                section.setVisibilityDataToOld();
            }
        }

//        if (time % 10 == 0) {
            for (Section[] sectionSlice : sections) {
                for (Section section : sectionSlice) {
                    section.removeDeadAgents();
                }
            }
//        }

        time++;
    }

    public void drawDebug() {
        for (Section[] sectionSlice : sections) {
            for (Section section : sectionSlice) {
                section.drawSectionDebug();
            }
        }
    }

    public Section getSection(int xID, int yID) {
        //Wrap around
        while (xID < 0) xID += sectionWidthCount;
        while (xID >= sectionWidthCount) xID -= sectionWidthCount;
        while (yID < 0) yID += sectionHeightCount;
        while (yID >= sectionHeightCount) yID -= sectionHeightCount;

//        if (sections[xID][yID] == null) {
//            System.out.println("WARNING: TRIED USING NULL SECTION");
//        }

        return sections[xID][yID];
    }

    public Section getSection(double x, double y) {
        int sectionXTemp = getSectionID(x);
        int sectionYTemp = getSectionID(y);

        return getSection(sectionXTemp, sectionYTemp);
    }

    public int getSectionID(double xy) {
        return (int) (xy / SECTION_SIZE);
    }

    public class Section {
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

        public Section(int xID, int yID) {
            this.xID = xID;
            this.yID = yID;
            this.agents = new ArrayList<>();
            this.plants = new ArrayList<>();
            this.spikes = new ArrayList<>();
            visibleAgents = new ArrayList<>();
            visiblePlants = new ArrayList<>();
            visibleSpikes = new ArrayList<>();
        }

        void drawSectionDebug() {
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

        void calculateAgentVisibilities() {
            visibleAgents.clear();
            visiblePlants.clear();
            visibleSpikes.clear();

            for (int xi = (-SECTION_VIEW_RANGE) + xID; xi <= SECTION_VIEW_RANGE + xID; xi++) {
                for (int yi = (-SECTION_VIEW_RANGE) + yID; yi <= SECTION_VIEW_RANGE + yID; yi++) {
                    visibleAgents.addAll(getSection(xi, yi).getAgents());
                    visiblePlants.addAll(getSection(xi, yi).getPlants());
                    visibleSpikes.addAll(getSection(xi, yi).getSpikes());
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

        public ArrayList<Agent> getVisibleAgents() {
            if (!visibleAgentsUpdated) {
                calculateAgentVisibilities();
            }
            return visibleAgents;
        }

        public ArrayList<Plant> getVisiblePlants() {
            if (!visiblePlantsUpdated) {
                calculateAgentVisibilities();
            }
            return visiblePlants;
        }

        public ArrayList<Spike> getVisibleSpikes() {
            if (!visibleSpikesUpdated) {
                calculateAgentVisibilities();
            }
            return visibleSpikes;
        }



    }
}
