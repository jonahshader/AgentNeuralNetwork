package MainParts;

import GamePieces.Agent;
import processing.core.PApplet;

/**
 * Created by Jonah on 3/27/2017.
 */
public class GameHUD {
    final static float Y_OFFSET = 32;
    AgentEvolution graphics;
    GameManager game;
    Agent spectatingAgent;

    public GameHUD(AgentEvolution graphics, GameManager game) {
        this.graphics = graphics;
        this.game = game;
    }

    public void drawHUD() {
        graphics.textSize(24);

        graphics.fill(0);
        graphics.text("Agents: " + game.getAgents().size(), 9, 33);
        graphics.text("Unused Energy: " + Math.round(game.getUnusedEnergy()), 9, 33 + Y_OFFSET);
        graphics.text("Force Reproduce Mode: " + (game.isForceReproduce() ? "ON" : "OFF"), 9, 33 + Y_OFFSET * 2);

        graphics.fill(255);
        graphics.text("Agents: " + game.getAgents().size(), 8, 32);
        graphics.text("Unused Energy: " + Math.round(game.getUnusedEnergy()), 8, 32 + Y_OFFSET);
        graphics.text("Force Reproduce Mode: " + (game.isForceReproduce() ? "ON" : "OFF"), 8, 32 + Y_OFFSET * 2);

        if (spectatingAgent != null) {
            if (spectatingAgent.isDead()) {
                spectatingAgent = null;
            } else {
                if (game.isDrawingBrain())
                    spectatingAgent.getBrain().drawNetwork(graphics, 400, 60);
            }
        }
        graphics.fill(80, 255, 80);
        graphics.stroke(0, 240, 0);
        polygon(graphics.width / 2, 30, 30, 3);  // Triangle
    }

    public void setSpectatingAgent(Agent spectatingAgent) {
        this.spectatingAgent = spectatingAgent;
    }

    private void polygon(float x, float y, float radius, int npoints) {
        float angle = PApplet.TWO_PI / npoints;
        graphics.beginShape();
        for (float a = 0; a < PApplet.TWO_PI; a += angle) {
            float sx = x + PApplet.cos(a) * radius;
            float sy = y + PApplet.sin(a) * radius;
            graphics.vertex(sx, sy);
        }
        graphics.endShape(PApplet.CLOSE);
    }

    public void mousePressed() {
        if (graphics.mouseX > (graphics.width / 2) - 30) {
            if (graphics.mouseX < (graphics.width / 2) + 30) {
                if (graphics.mouseY < 60) {
                    game.startGame();
                    System.out.println("Game started!");
                }
            }
        }
    }
}
