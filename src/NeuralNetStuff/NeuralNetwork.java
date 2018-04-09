package NeuralNetStuff;

import processing.core.PApplet;

/**
 * Created by loshaderj16 on 3/14/2017.
 */
public class NeuralNetwork {
    final static double INIT_WEIGHT_POW = 1;
    final static double INIT_WEIGHT_SCALE = 0.4f;
    final static double MUTATION_POW = 1;
    final static double MUTATION_SCALE = 0.01; //0.001
    final static double MUTATION_CHANCE = 1; //0.01

    final static double WEIGHT_MUTABILITY_MUTATION_RATE = 0.075;

    //Graphics constants
    final static double BRIGHTNESS_MULTIPLIER = 255;
    final static int WIDTH_SPACING = 400;
    final static int HEIGHT_SPACING = 25;
    final static float NEURON_SIZE = 7;
    final static float LAYER_HEIGHT = 25;

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
            graphics.stroke((float) ((1 + inputs[i]) * BRIGHTNESS_MULTIPLIER));
            graphics.fill((float) ((1 + inputs[i]) * BRIGHTNESS_MULTIPLIER));

            graphics.ellipse(x - WIDTH_SPACING, y + i * HEIGHT_SPACING * LAYER_HEIGHT / inputs.length, NEURON_SIZE, NEURON_SIZE);
        }
    }

    public void printDebug() {
        System.out.println("Neural net debug: ");
        for (int i = 0; i < layers.length; i++) {
            layers[i].printDebug();
        }
    }

    class Layer{
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
                    weights[i][j] = ranFlip(Math.pow(Math.random(), INIT_WEIGHT_POW)) * INIT_WEIGHT_SCALE;
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
            for (int i = 0; i < inputs + 1; i++) { //TODO: possibly should be inputs + 1 instead of inputs. not sure if
                for (int j = 0; j < outputs; j++) {
                    if (Math.random() < MUTATION_CHANCE) {
//                        System.out.print("Mutated weight " + weights[i][j]);
                        weights[i][j] += ranFlip(ranPow(MUTATION_POW)) * MUTATION_SCALE * mutationRate * weightMutability[i][j];
                        weightMutability[i][j] += ranFlip(ranPow(MUTATION_POW)) * WEIGHT_MUTABILITY_MUTATION_RATE * mutationRate;
//                        System.out.println(" to " + weights[i][j]);
                    }
                }
            }
        }

        public void drawLayer(PApplet graphics, float x, float y) {
            for (int i = 0; i < inputs + 1; i++) {
                for (int j = 0; j < outputs; j++) {
                    if (Math.abs(weights[i][j]) > 0.1) {
                        graphics.stroke((float) ((weights[i][j]) * BRIGHTNESS_MULTIPLIER), 0, (float) ((-weights[i][j]) * BRIGHTNESS_MULTIPLIER), (float) Math.abs(weights[i][j] * 255.0));
                        graphics.line(x - WIDTH_SPACING, y + (i * HEIGHT_SPACING * LAYER_HEIGHT) / inputs, x, y + j * HEIGHT_SPACING * LAYER_HEIGHT / outputs);
                    }
                }
            }

            for (int j = 0; j < neurons.length; j++) {
                graphics.fill((float) ((1 + neurons[j].getOutput()) * BRIGHTNESS_MULTIPLIER));
                graphics.stroke((float) ((1 + neurons[j].getOutput()) * 255));
                graphics.ellipse(x, y + j * HEIGHT_SPACING * LAYER_HEIGHT / outputs, NEURON_SIZE, NEURON_SIZE);
            }
        }

        public void drawWeightMutability(PApplet graphics, float x, float y) {
            for (int i = 0; i < inputs + 1; i++) {
                for (int j = 0; j < outputs; j++) {
                    graphics.textSize(14);
                    graphics.fill((float) ((weightMutability[i][j]/2.0) * BRIGHTNESS_MULTIPLIER));
//                    graphics.line(x - WIDTH_SPACING, y + (i * HEIGHT_SPACING * LAYER_HEIGHT) / inputs, x, y + j * HEIGHT_SPACING * LAYER_HEIGHT / outputs);
                    float textX = ((x - WIDTH_SPACING) + x) / 2.0f;
                    float textY = ((y + (i * HEIGHT_SPACING * LAYER_HEIGHT) / inputs) + (y + j * HEIGHT_SPACING * LAYER_HEIGHT / outputs)) / 2.0f;
                    graphics.text(PApplet.nf((float) weightMutability[i][j], 1, 2), textX, textY);
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
}
