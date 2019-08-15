package NeuralNetStuff;

import MainParts.GlobalRandom;
import processing.core.PApplet;

import java.io.Serializable;

import static NeuralNetStuff.NeuralNetwork.*;

public class Layer implements Serializable {
    //a layer contains weights and output neurons.
    int inputs, outputs;
    double[][] weights; //1st [] inputs, 2nd [] outputs (when used in a for loop)
    double[][] weightMutability;
    double[] inputValues;
    Neuron[] neurons;

    Layer(int inputs, int outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
        neurons = new Neuron[outputs];
        for (int i = 0; i < outputs; i++) {
            neurons[i] = new Neuron(0.0);
        }
        weights = new double[inputs + 1][outputs];  //The +1 is for the bias neuron
        weightMutability = new double[inputs + 1][outputs];
        inputValues = new double[inputs];

        for (int i = 0; i < inputs + 1; i++) {      //Randomly set weights
            for (int j = 0; j < outputs; j++) {
//                    weights[i][j] = GlobalRandom.random.nextGaussian() / Math.pow(((inputs + 1) * outputs) / INIT_WEIGHT_ACTIVITY, .5);
                weights[i][j] = GlobalRandom.random.nextGaussian() * INIT_WEIGHT_ACTIVITY;
                weightMutability[i][j] = 1; //Default to 1 so all weights are mutable
            }
        }
    }

    Layer(Layer parentLayer) {
        this.inputs = parentLayer.inputs;
        this.outputs = parentLayer.outputs;
        neurons = new Neuron[outputs];
        for (int i = 0; i < outputs; i++) {
            neurons[i] = new Neuron(0.0);
        }

        weights = new double[inputs + 1][outputs];
        weightMutability = new double[inputs + 1][outputs];
        inputValues = new double[inputs];
        for (int i = 0; i < inputs + 1; i++) {
            for (int j = 0; j < outputs; j++) {
                weights[i][j] = parentLayer.weights[i][j];
                weightMutability[i][j] = parentLayer.weightMutability[i][j];
            }
        }
    }

    public void setInput(int index, double value) {
        inputValues[index] = value;
    }

    public void calculate() {
        for (int i = 0; i < neurons.length; i++) {
            double total = 0;                         //Sum of
            for (int j = 0; j < inputValues.length; j++) {
                total += inputValues[j] * weights[j][i];
            }
            total += 1.0 * weights[inputs][i];  //Bias neuron

            neurons[i].setInput(total);

        }
    }

    public double getOutput(int neuronIndex) {
        return neurons[neuronIndex].getOutput();
    }

    public double getRawOutput(int neuronIndex) {
        return neurons[neuronIndex].getRawOutput();
    }

    public void mutateWeights(double mutationRate) {
        for (int i = 0; i < inputs + 1; i++) {
            for (int j = 0; j < outputs; j++) {
                weights[i][j] += GlobalRandom.random.nextGaussian() * MUTATION_SCALE * mutationRate;
                weightMutability[i][j] += ranFlip(ranPow(MUTATION_POW)) * WEIGHT_MUTABILITY_MUTATION_RATE * mutationRate;
            }
        }
    }

    public void drawLayer(PApplet graphics, float x, float y) {
        for (int i = 0; i < inputs + 1; i++) {
            for (int j = 0; j < outputs; j++) {
                if (Math.abs(weights[i][j]) > 0.75) {
                    graphics.stroke((float) ((weights[i][j]) * BRIGHTNESS_MULTIPLIER), 0, (float) ((-weights[i][j]) * BRIGHTNESS_MULTIPLIER), (float) Math.abs(weights[i][j] * 255.0));
                    graphics.line(x - WIDTH_SPACING, y + (i * HEIGHT_SPACING * LAYER_HEIGHT) / inputs, x, y + j * HEIGHT_SPACING * LAYER_HEIGHT / outputs);
                }
            }
        }

        for (int j = 0; j < neurons.length; j++) {
            graphics.fill((float) ((0.5 + neurons[j].getOutput()) * BRIGHTNESS_MULTIPLIER));
            graphics.stroke((float) ((0.5 + neurons[j].getOutput()) * BRIGHTNESS_MULTIPLIER));
            graphics.ellipse(x, y + j * HEIGHT_SPACING * LAYER_HEIGHT / outputs, NEURON_SIZE, NEURON_SIZE);
        }
    }

    public void drawWeightMutability(PApplet graphics, float x, float y) {
        for (int i = 0; i < inputs + 1; i++) {
            for (int j = 0; j < outputs; j++) {
                if (Math.abs(weightMutability[i][j] - 1) > 0.5) {
                    graphics.textSize(14);
                    graphics.fill((float) ((weightMutability[i][j]/2.0) * BRIGHTNESS_MULTIPLIER));
//                    graphics.line(x - WIDTH_SPACING, y + (i * HEIGHT_SPACING * LAYER_HEIGHT) / inputs, x, y + j * HEIGHT_SPACING * LAYER_HEIGHT / outputs);
                    float textX = ((x - WIDTH_SPACING) + x) / 2.0f;
                    float textY = ((y + (i * HEIGHT_SPACING * LAYER_HEIGHT) / inputs) + (y + j * HEIGHT_SPACING * LAYER_HEIGHT / outputs)) / 2.0f;
                    graphics.text(PApplet.nf((float) weightMutability[i][j], 1, 2), textX, textY);
                }
            }
        }
    }

    public void printDebug() {
        System.out.println("    layer debug: " );
        for (int i = 0; i < inputs + 1; i++) {
            for (int j = 0; j < outputs; j++) {
                System.out.println("        " + i + " " + j + " weight mutability: " + weightMutability[i][j]);
            }
        }
    }
}