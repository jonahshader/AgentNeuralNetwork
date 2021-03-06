package MainParts;

import GamePieces.Agent;
import processing.core.PApplet;

import java.io.Serializable;

/**
 * Created by Jonah on 3/27/2017.
 */
class GameHUD implements Serializable {
    private final static float Y_OFFSET = 32;
    private GameManager game;
    private Agent spectatingAgent;

    GameHUD(GameManager game) {
        this.game = game;
    }

    void drawHUD(PApplet graphics) {
        graphics.textSize(24);

        graphics.fill(0);
        graphics.text("Agents: " + game.getAgents().size(), 9, 33);
        graphics.text("Unused Energy: " + Math.round(game.getUnusedEnergy()), 9, 33 + Y_OFFSET);
//        graphics.text("Total Energy:   " + Math.round(game.calculateTotalEnergy()), 9, 33 + Y_OFFSET * 2);
        graphics.text("Force Reproduce Mode: " + (game.isForceReproduce() ? "ON" : "OFF"), 9, 33 + Y_OFFSET * 3);

        graphics.fill(255);
        graphics.text("Agents: " + game.getAgents().size(), 8, 32);
        graphics.text("Unused Energy: " + Math.round(game.getUnusedEnergy()), 8, 32 + Y_OFFSET);
//        graphics.text("Total Energy:   " + Math.round(game.calculateTotalEnergy()), 8, 32 + Y_OFFSET * 2);
        graphics.text("Force Reproduce Mode: " + (game.isForceReproduce() ? "ON" : "OFF"), 8, 32 + Y_OFFSET * 3);

        if (spectatingAgent != null) {
            if (spectatingAgent.isDead()) {
                spectatingAgent = null;
            } else {
                if (game.isDrawingBrain())
                    spectatingAgent.getBrain().drawNetwork(graphics, 400, 60);
                spectatingAgent.calculateTotalEnergy(false);
            }
        }
    }

    void setSpectatingAgent(Agent spectatingAgent) {
        this.spectatingAgent = spectatingAgent;
    }

    private void polygon(float x, float y, float radius, int npoints, PApplet graphics) {
        float angle = PApplet.TWO_PI / npoints;
        graphics.beginShape();
        for (float a = 0; a < PApplet.TWO_PI; a += angle) {
            float sx = x + PApplet.cos(a) * radius;
            float sy = y + PApplet.sin(a) * radius;
            graphics.vertex(sx, sy);
        }
        graphics.endShape(PApplet.CLOSE);
    }

    void mousePressed() {

    }
}
