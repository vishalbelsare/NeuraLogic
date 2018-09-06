package constructs.example;

import constructs.Conjunction;
import constructs.template.WeightedRule;
import grounding.GroundTemplate;
import networks.structure.NeuralNetwork;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * Lifted trainExamples are structures that share common template part through learning (just like the regular trainExamples),
 * but also have extra parts that are unique to them. This decomposes the template into relevant subparts,
 * instead of having one huge template carrying data for all the trainExamples.
 * <p>
 * Created by gusta on 13.3.17.
 */
public class LiftedExample extends GroundExample {
    public LinkedHashSet<WeightedRule> rules;

    @Nullable
    GroundTemplate groundTemplate; //todo next - propagate this through for shared grounding with parallel shuffling (rather create new Object that encompasses these two in sample)

    public LiftedExample() {
    }

    public LiftedExample(List<Conjunction> conjunctions, List<WeightedRule> irules) {
        super(conjunctions);
        rules = new LinkedHashSet<>();
        rules.addAll(irules);

    }
}