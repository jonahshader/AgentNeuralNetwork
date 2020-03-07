package NeuralNetStuff;

import MainParts.GlobalRandom;
import processing.core.PApplet;

import java.io.Serializable;
import java.util.Random;

/**
 * Created by loshaderj16 on 3/14/2017.
 */
public class NeuralNetwork implements Serializable {
    final static double INIT_WEIGHT_POW = 1;
    final static double INIT_WEIGHT_SCALE = 0.4f;
    final static double INIT_WEIGHT_ACTIVITY = 1/4.0; //23
    final static double MUTATION_POW = 1;
    final static double MUTATION_SCALE = 0.015; //0.03
    final static double MUTATION_CHANCE = 1; //0.01

    final static double WEIGHT_MUTABILITY_MUTATION_RATE = 0.01; //0.075

    //Graphics constants
    final static double BRIGHTNESS_MULTIPLIER = 255;
    final static int WIDTH_SPACING = 400;
    final static int HEIGHT_SPACING = 25;
    final static float NEURON_SIZE = 7;
    final static float LAYER_HEIGHT = 35;

    double[] inputs;
    double[] outputs;
    Layer[] layers;

    //NOTE: the length of input determines how many dud neurons there will be for inputs.
    //if input changes after creation, the neural network will not add new weights or remove weights
    public NeuralNetwork(int inputSize, int outputSize, int[] hiddenLayerSize) {
        inputs = new double[inputSize];
        outputs = new double[outputSize];
        int layerCount = hiddenLayerSize.length + 1;
        layers = new Layer[layerCount]; //the + 1 is for the output layer


        //Create hidden layers
        for (int i = 0; i < layerCount; i++) {
            if (i > 0 && i < layerCount - 1) {                          //Middle layers's input size is the previous layer's output size.
                layers[i] = new Layer(hiddenLayerSize[i - 1], hiddenLayerSize[i]);
            } else if (i == 0) {                                                    //First layer's input size = the neural network's input size
                layers[0] = new Layer(inputSize, hiddenLayerSize[0]);
            } else {                                                                //Last layer's output size = the neural network's output size
                layers[i] = new Layer(hiddenLayerSize[i - 1], outputSize);
            }
        }
    }

    public NeuralNetwork(NeuralNetwork parentNetwork) {
        inputs = parentNetwork.inputs.clone();
        outputs = parentNetwork.outputs.clone();
        layers = new Layer[parentNetwork.layers.length];
        for (int i = 0; i < parentNetwork.layers.length; i++) {
            layers[i] = new Layer(parentNetwork.layers[i]);                         //Clone layers
        }
    }

    public void mutateNetWeights(double mutationRate) {
        for (Layer layer : layers) {
            layer.mutateWeights(mutationRate);
        }
    }

    public void setInput(int index, double value) {
        inputs[index] = value;
    }

    public double getOutput(int index) {
        return outputs[index];
    }

    public void calculateNet() {
        for (int i = 0; i < inputs.length; i++) {
            layers[0].setInput(i, inputs[i]);
        }
        layers[0].calculate();

        for (int i = 1; i < layers.length; i++) {
            for (int j = 0; j < layers[i].inputs; j++) {
                layers[i].setInput(j, layers[i - 1].getOutput(j));
            }

            layers[i].calculate();
        }

        //last layer
        for (int j = 0; j < layers[layers.length - 1].outputs; j++) {
            outputs[j] = layers[layers.length - 1].getRawOutput(j);
        }
    }

    public static double ranPow(double pow) {
        return Math.pow(Math.random(), pow);
    }

    public static double ranFlip(double value) {
        return Math.random() > 0.5 ? value : -value;
    }

    /**
     * @param value
     * @return a randome value from -value to value
     */
    public static double ranRange(double value) {
        return ranFlip(Math.random() * value);
    }

    public void drawNetwork(PApplet graphics, float x, float y) {
        for (int i = layers.length - 1; i >= 0; i--) {
            layers[i].drawLayer(graphics, x + i * WIDTH_SPACING, y);
        }

        for (int i = layers.length - 1; i >= 0; i--) {
            layers[i].drawWeightMutability(graphics, x + i * WIDTH_SPACING, y);
        }

        for (int i = 0; i < inputs.length; i++) {
            graphics.stroke((float) ((0.5 + inputs[i]) * BRIGHTNESS_MULTIPLIER));
            graphics.fill((float) ((0.5 + inputs[i]) * BRIGHTNESS_MULTIPLIER));

            graphics.ellipse(x - WIDTH_SPACING, y + i * HEIGHT_SPACING * LAYER_HEIGHT / inputs.length, NEURON_SIZE, NEURON_SIZE);
        }
    }

    public void printDebug() {
        System.out.println("Neural net debug: ");
        for (int i = 0; i < layers.length; i++) {
            layers[i].printDebug();
        }
    }
}
