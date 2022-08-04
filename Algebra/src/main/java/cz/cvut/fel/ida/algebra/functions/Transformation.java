package cz.cvut.fel.ida.algebra.functions;

import cz.cvut.fel.ida.algebra.functions.transformation.joint.*;
import cz.cvut.fel.ida.algebra.values.Value;
import cz.cvut.fel.ida.setup.Settings;
import cz.cvut.fel.ida.utils.exporting.Exportable;

import java.util.logging.Logger;

/**
 * Class representing (arbitrary) transformation of a single Value to a single Value
 */
public interface Transformation extends ActivationFcn, Exportable {

    static final Logger LOG = Logger.getLogger(Transformation.class.getName());

    /**
     * The evaluation operates on a single input Value
     *
     * @param combinedInputs
     * @return
     */
    public abstract Value evaluate(Value combinedInputs);

    /**
     * The differentiation returns a single output Value
     *
     * @param combinedInputs
     * @return
     */
    public abstract Value differentiate(Value combinedInputs);

    public static Transformation getFunction(Settings.TransformationFcn transformation) {
        ElementWise function = ElementWise.getFunction(transformation);
        if (function != null) {
            return function;
        }
        switch (transformation) {
            case TRANSP:
                return Singletons.transposition;
            case SOFTMAX:
                return Singletons.softmax;
            case SPARSEMAX:
                return Singletons.sparsemax;
            default:
                LOG.severe("Unimplemented Transformation function");
                return null;
        }
    }

    public static class Singletons {
        public static Softmax softmax = new Softmax();
        public static Sparsemax sparsemax = new Sparsemax();

        public static Transposition transposition = new Transposition();
    }


    public static abstract class State implements ActivationFcn.State {

        Transformation transformation;

        protected Value input;
        protected Value processedGradient;

        public State(Transformation transformation){
            this.transformation = transformation;
        }

        @Override
        public void cumulate(Value value) {
            input = value;  // there should be only a single input value for this state type!!
        }

        @Override
        public void invalidate() {
            input = null;
            processedGradient = null;
        }

        @Override
        public Value evaluate() {
            return transformation.evaluate(input);
        }

        public Value gradient() {
            return transformation.differentiate(input);
        }

        @Override
        public void ingestTopGradient(Value topGradient) {
            Value inputFcnDerivative = gradient();
            processedGradient = inputFcnDerivative.times(topGradient);  //times here - since the fcn was a complex vector function (e.g. softmax) and has a matrix derivative (Jacobian)
        }

        @Override
        public Value nextInputDerivative() {
            return processedGradient;
        }

        @Override
        public void setupDimensions(Value value) {
            this.input = value.getForm();
        }

        @Override
        public Transformation getTransformation() {
            return transformation;
        }

        @Override
        public void setTransformation(Transformation transformation) {
            this.transformation = transformation;
        }

        @Override
        public Combination getCombination() {
            return null;
        }

        @Override
        public void setCombination(Combination combination) {
            LOG.severe("Trying to set Combination in Transformation.State");
        }
    }


}