package MainParts;

import GamePieces.Agent;

/**
 * Created by Jonah on 4/9/2017.
 */

//TODO: finish this
//TODO: this was a bad idea. butcher almost everything in here
public class Modes {
    //How difficult the
    public enum Mode {
        PLANTS, NO_PLANTS
    }

    public enum Food {
        LITTLE, MEDIUM, LOTS
    }

    //----------------------------PARAMETERS-----------------------------

    //GameManager parameters
    private static int worldWidth = 15000;
    private static int worldHeight = 12500;
    private static int startingAgentCount = 1000;
    private static int startingPlantCount = 500;
    private static int startingSpikeCount = 300;
    private static int minimumAgentCount = 250;
    private static double minimumStartingAgentEnergy = 1200f;
    private static double startingEnergyBoostScale = (Agent.MIN_REPRODUCE_ENERGY / minimumStartingAgentEnergy) * 0.5;

    //Agent parameters
    private static double agentSizeScale = 1f;
    private static double carnivoreConsumeRatio = 0.8f;
    private static double turnFoodCost = 40f;
    private static double idleFoodCost = 0.075f;
    private static double moveFoodCost = 0.005f;
    private static double energyDiameterScale = 0.01f;
    private static double energyConsumptionSizePow = 1.5f;
    private static double energyConsumptionOverallPow = 1.05f;
    private static double eatAgentEnergyScale = 0.5f;
    private static double eatAgentEffectiveSizeScale = 0.3f;
    private static double eatPlantEffectiveSizeScale = 1.1f;
    private static double minReproduceEnergy = 2000f;

    //Agent sensor parameters
    private static double eyeAngleWidth = Math.PI / 12;
    private static int eyeCount = 5;
    private static double eyeLengthScale = 4f;
    private static int feedbackNeurons = 0;
    private static int miscInputCount = 8;
    private static int miscOutputCount = 7;

    //VisionOptimiser parameters
    private static int sectionSize = 256;
    private static int sectionViewRange = 1;

    //Plant parameters
    private static int baseColorRed = 40;
    private static int baseColorGreen = 220;
    private static int baseColorBlue = 25;
    private static double baseColorVariance = 0.3f;
    private static double maxGrowRate = 1.1f;
    private static double plantSizeScale = 3.5f;

    //Environment parameters
    private static double dayNightCycleLength = 10000;
    private static double maxBrightness = 100;
    private static double minBrightness = 50;

    //GameHUD parameters
    private static double yOffset = 48;

    //NeuralNetwork
    private static double initWeightPow = 5;
    private static double initWeightScale = 1.0;
    private static double mutationPow = 15;
    private static double mutationScale = 1;
    private static double mutationChance = 0.01;
    //-----------------------------------------------------------------------

    //Scales up or down everything (world size, population, food, NOT difficulty)
    private static double scale = 1.0f;

    public static int getWorldWidth() {
        return (int) (worldWidth * Math.sqrt(scale));
    }

    public static int getWorldHeight() {
        return (int) (worldHeight * Math.sqrt(scale));
    }

    public static int getStartingAgentCount() {
        return (int) (startingAgentCount * scale);
    }

    public static int getStartingPlantCount() {
        return (int) (startingPlantCount * scale);
    }

    public static int getStartingSpikeCount() {
        return (int) (startingSpikeCount * scale);
    }

    public static int getMinimumAgentCount() {
        return (int) (minimumAgentCount * scale);
    }

    public static double getMinimumStartingAgentEnergy() {
        return minimumStartingAgentEnergy;
    }

    public static double getStartingEnergyBoostScale() {
        return startingEnergyBoostScale;
    }

    public static double getAgentSizeScale() {
        return agentSizeScale;
    }

    public static double getCarnivoreConsumeRatio() {
        return carnivoreConsumeRatio;
    }

    public static double getTurnFoodCost() {
        return turnFoodCost;
    }

    public static double getIdleFoodCost() {
        return idleFoodCost;
    }

    public static double getMoveFoodCost() {
        return moveFoodCost;
    }

    public static double getEnergyDiameterScale() {
        return energyDiameterScale;
    }

    public static double getEnergyConsumptionSizePow() {
        return energyConsumptionSizePow;
    }

    public static double getEnergyConsumptionOverallPow() {
        return energyConsumptionOverallPow;
    }

    public static double getEatAgentEnergyScale() {
        return eatAgentEnergyScale;
    }

    public static double getEatAgentEffectiveSizeScale() {
        return eatAgentEffectiveSizeScale;
    }

    public static double getEatPlantEffectiveSizeScale() {
        return eatPlantEffectiveSizeScale;
    }

    public static double getMinReproduceEnergy() {
        return minReproduceEnergy;
    }

    public static double getEyeAngleWidth() {
        return eyeAngleWidth;
    }

    public static int getEyeCount() {
        return eyeCount;
    }

    public static double getEyeLengthScale() {
        return eyeLengthScale;
    }

    public static int getFeedbackNeurons() {
        return feedbackNeurons;
    }

    public static int getMiscInputCount() {
        return miscInputCount;
    }

    public static int getMiscOutputCount() {
        return miscOutputCount;
    }

    public static int getSectionSize() {
        return sectionSize;
    }

    public static int getSectionViewRange() {
        return sectionViewRange;
    }

    public static int getBaseColorRed() {
        return baseColorRed;
    }

    public static int getBaseColorGreen() {
        return baseColorGreen;
    }

    public static int getBaseColorBlue() {
        return baseColorBlue;
    }

    public static double getBaseColorVariance() {
        return baseColorVariance;
    }

    public static double getMaxGrowRate() {
        return maxGrowRate;
    }

    public static double getPlantSizeScale() {
        return plantSizeScale;
    }

    public static double getDayNightCycleLength() {
        return dayNightCycleLength;
    }

    public static double getMaxBrightness() {
        return maxBrightness;
    }

    public static double getMinBrightness() {
        return minBrightness;
    }

    public static double getyOffset() {
        return yOffset;
    }

    public static double getInitWeightPow() {
        return initWeightPow;
    }

    public static double getInitWeightScale() {
        return initWeightScale;
    }

    public static double getMutationPow() {
        return mutationPow;
    }

    public static double getMutationScale() {
        return mutationScale;
    }

    public static double getMutationChance() {
        return mutationChance;
    }

    public static void setDifficultyMode(Mode mode) {
        switch (mode) {
            case PLANTS:
                startingPlantCount = 1000; //TODO: separate startingPlantCount initial value into a constant
                break;
            case NO_PLANTS:
//                System.out.println(startingAgentCount);
//                System.out.println(minimumAgentCount);
                minimumAgentCount = startingAgentCount;
                minimumAgentCount *= 4;
                startingAgentCount *= 4;
                startingPlantCount = 0;
                break;
            default:
                break;
        }
    }

    public static void setScale(double scale) {
        Modes.scale = scale;
    }

    public static void disableSpikes() {
//        if (disable) {
            startingSpikeCount = 0;
//        }
    }
}
