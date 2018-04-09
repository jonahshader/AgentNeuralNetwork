package GamePieces.AgentParts;

import GamePieces.Agent;
import MainParts.Environment;

/**
 * Created by Jonah on 5/13/2017.
 */
public class AgentMouth {
    Agent parentAgent;
    Environment env;
    double diameter;
    double foodPreference; //Ratio of effectiveness between plant and agent. 1 = 100% effectiveness against food and 0% effectiveness against agent.
    double relativeAngle; //Angle difference from center line
    double surfaceDistance; //Distance away from agent surface.
    double x, y;

    public AgentMouth(Agent parentAgent, double foodPreference, double relativeAngle, double surfaceDistance) {
        this.parentAgent = parentAgent;
        this.foodPreference = foodPreference;
        this.relativeAngle = relativeAngle;
        this.surfaceDistance = surfaceDistance;
    }

//    public updateLocation
}
