package VisionOptimisation;

import processing.core.PApplet;

import java.io.Serializable;

/**
 * Created by Jonah on 3/26/2017.
 */
public class VisionOptimiser implements Serializable {
    final static int SECTION_SIZE = 350;///256
    final static int SECTION_VIEW_RANGE = 1; // additional sections in all directions the content can react to. should be at least 1

    private Section[][] sections;
    private int sectionWidthCount;
    private int sectionHeightCount;

    public VisionOptimiser(int worldWidth, int worldHeight) {

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

        for (Section[] sectionSlice : sections) {
            for (Section section : sectionSlice) {
                section.removeDeadAgents();
            }
        }
    }

    public void drawDebug(PApplet graphics) {
        for (Section[] sectionSlice : sections) {
            for (Section section : sectionSlice) {
                section.drawSectionDebug(graphics);
            }
        }
    }

    public Section getSection(int xID, int yID) {
        //Wrap around
        while (xID < 0) xID += sectionWidthCount;
        while (xID >= sectionWidthCount) xID -= sectionWidthCount;
        while (yID < 0) yID += sectionHeightCount;
        while (yID >= sectionHeightCount) yID -= sectionHeightCount;

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
}
