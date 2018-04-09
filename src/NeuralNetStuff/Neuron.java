package NeuralNetStuff;

/**
 * Created by Jonah on 3/27/2017.
 */
public class Neuron {
    private double input;

    public Neuron(double input) {
        this.input = input;
    }

    public double getOutput() {
//        return rprelu(input);
        return prelu(input);
//        return Math.tanh(input);
//        return Math.sin(input) + input;
//        return Math.pow(input, 2);
//        return sigmoid(input);
    }

    public double getRawOutput() {
        return input;
    }

    public void setInput(double value) {
        input = value;
    }
    //Activation functions
    private double sigmoid(double value) {
        return 1.0 / (1.0 + Math.pow(Math.E, -value));
    }

    private double prelu(double value) {
        return value > 0.0 ? value : value / 50.0;
    }

    private double rprelu(double value) {
        return value > 0.0 ? value : value / (50.0 * (Math.random() + 0.5));
    }
}
