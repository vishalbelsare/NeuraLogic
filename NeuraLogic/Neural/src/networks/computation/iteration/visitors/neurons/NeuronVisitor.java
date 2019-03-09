package networks.computation.iteration.visitors.neurons;

import com.sun.istack.internal.Nullable;
import networks.computation.iteration.IterationStrategy;
import networks.computation.iteration.visitors.states.StateVisiting;
import networks.computation.iteration.visitors.weights.WeightUpdater;
import networks.structure.components.NeuralNetwork;
import networks.structure.components.neurons.BaseNeuron;
import networks.structure.components.neurons.Neuron;
import networks.structure.components.neurons.WeightedNeuron;
import networks.structure.metadata.states.State;

/**
 * Abstract representation for all the actions performed on the level of Neurons while iterating ({@link IterationStrategy}.
 * This is a pure container for the action, completely decoupled from the logic of iteration, i.e. no expansion of nodes
 * for iteration should happen here, just an immediate propagation of messages to neighbours (PureVisitor).
 * The logic of the calculation upon the {@link State} of each neuron is further outsourced to {@link #stateVisitor}
 * for extra composition of possible iteration logic.
 *
 * @see StateVisiting
 * @see Neuron
 * @see BaseNeuron
 */
public abstract class NeuronVisitor {

    /**
     * We need the context of the network even at the level of visiting individual neurons to ask for the inputs properly.
     */
    NeuralNetwork<State.Neural.Structure> network;
    /**
     * The logic of the calculation upon the {@link State} of each neuron is further outsourced to {@link #stateVisitor}
     */
    public StateVisiting.Computation stateVisitor;   //todo change to generic StateVisitor?

    public NeuronVisitor(NeuralNetwork<State.Neural.Structure> network, StateVisiting.Computation computationVisitor) {
        this.network = network;
        this.stateVisitor = computationVisitor;
    }

    /**
     * Expand neighbours, Propagate result of this neuron into the neighbours (inputs or outputs).
     *
     * @param neuron
     */
    public abstract <T extends Neuron, S extends State.Neural> void visit(BaseNeuron<T, S> neuron);


    public abstract static class Weighted extends NeuronVisitor {

        /**
         * Takes care of the weight update during neuron visit.
         */
        @Nullable
        public WeightUpdater weightUpdater;

        public Weighted(NeuralNetwork<State.Neural.Structure> network, StateVisiting.Computation computationVisitor, @Nullable WeightUpdater weightUpdater) {
            super(network, computationVisitor);
            this.weightUpdater = weightUpdater;
        }

        /**
         * Expand neighbours and Propagate result of this neuron into the neighbours (inputs or outputs) with the corresponding edge weight.
         * Also record an update for the weight in the process.
         *
         * @param neuron
         */
        public abstract <T extends Neuron, S extends State.Neural> void visit(WeightedNeuron<T, S> neuron);

        public abstract static class Indexed extends Weighted {

            public Indexed(NeuralNetwork<State.Structure> network, StateVisiting.Computation computationVisitor, @Nullable WeightUpdater weightUpdater) {
                super(network, computationVisitor, weightUpdater);
            }

            public void visit(WeightedNeuron neuron) {
                visit(neuron, 0);
            }

            public void visit(BaseNeuron neuron) {
                visit(neuron, 0);
            }

            abstract void visit(BaseNeuron neuron, int index);

            abstract void visit(WeightedNeuron neuron, int index);

        }

    }
}