package grounding;

import constructs.example.QueryAtom;
import constructs.example.ValuedFact;
import constructs.template.BodyAtom;
import constructs.template.WeightedRule;
import constructs.template.templates.GraphTemplate;
import ida.ilp.logic.Clause;
import ida.ilp.logic.Literal;
import ida.ilp.logic.subsumption.Matching;
import learning.Example;
import networks.structure.NeuralNetwork;
import networks.structure.lrnnTypes.AggregationNeuron;
import networks.structure.lrnnTypes.AtomNeuron;
import networks.structure.lrnnTypes.FactNeuron;
import networks.structure.lrnnTypes.RuleNeuron;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Temporary structure created during grounding for transfer between ground Herbrand model (rules+facts) and neural network.
 */
public class GroundTemplate extends GraphTemplate implements Example {
    String id;

    /**
     * Temp (for current pair of Template+Example) structure (head -> rules -> ground bodies) for traversing the graph of groundings
     */
    @NotNull
    public LinkedHashMap<Literal, LinkedHashMap<WeightedRule, LinkedHashSet<WeightedRule>>> groundRules;

    /**
     * Temp (for current pair of Template+Example) set of true ground facts
     */
    @NotNull
    public Map<Literal, ValuedFact> groundFacts;

    public NeuronMaps neuronMaps;

    public static class NeuronMaps {
        Map<Literal, AtomNeuron> atomNeurons = new HashMap<>();
        Map<WeightedRule, AggregationNeuron> aggNeurons = new HashMap<>();
        Map<WeightedRule, RuleNeuron> ruleNeurons = new LinkedHashMap<>();
        Map<Literal, FactNeuron> factNeurons = new HashMap<>();

        public void addAllFrom(NeuronMaps neuronMaps) {
            atomNeurons.putAll(neuronMaps.atomNeurons);
            aggNeurons.putAll(neuronMaps.aggNeurons);
            ruleNeurons.putAll(neuronMaps.ruleNeurons);
            factNeurons.putAll(neuronMaps.factNeurons);
        }
    }

    @Nullable
    NeuralNetwork neuralNetwork;


    public GroundTemplate() {

    }

    public GroundTemplate(LinkedHashMap<Literal, LinkedHashMap<WeightedRule, LinkedHashSet<WeightedRule>>> groundRules, Map<Literal, ValuedFact> groundFacts) {
        this.groundRules = groundRules;
        this.groundFacts = groundFacts;
        this.neuronMaps = new NeuronMaps();
    }

    public GroundTemplate(GroundTemplate other) {
        this.groundRules = other.groundRules;
        this.groundFacts = other.groundFacts;
        this.neuronMaps = other.neuronMaps;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Integer getSize() {
        return groundRules.size();
    }


    public GroundTemplate diffAgainst(GroundTemplate reuse) {

        for (Map.Entry<Literal, LinkedHashMap<WeightedRule, LinkedHashSet<WeightedRule>>> entry : reuse.groundRules.entrySet()) {
            for (Map.Entry<WeightedRule, LinkedHashSet<WeightedRule>> entry2 : entry.getValue().entrySet()) {
                for (WeightedRule rule : entry2.getValue()) {
                    //delete pointers to the newly proved rules which are equivalent the the previously proved rules
                    LinkedHashSet<WeightedRule> rules = this.groundRules.get(entry.getKey()).get(entry2.getKey());
                    rules.remove(rule); //todo change to factory method which tells if new instead and go back to arraylist instead of LinkedHashSet for the groundings?(will be faster?)
                }
            }
        }
        //also forget newly proved equivalent facts
        this.groundFacts.keySet().removeAll(reuse.groundFacts.keySet());

        //but take all the previously created neurons
        this.neuronMaps.addAllFrom(reuse.neuronMaps);

        //todo next - return new GroundTempalte as diff
    }

    public GroundTemplate prune(QueryAtom queryAtom) {
        GroundTemplate groundTemplate = new GroundTemplate(this);
        LinkedHashMap<Literal, LinkedHashMap<WeightedRule, LinkedHashSet<WeightedRule>>> support = new LinkedHashMap<>();
        Matching matching = new Matching();
        for (Map.Entry<Literal, LinkedHashMap<WeightedRule, LinkedHashSet<WeightedRule>>> entry : groundRules.entrySet()) {

            if (queryAtom.headAtom.literal.predicate().equals(entry.getKey().predicate()) && matching.subsumption(new Clause(queryAtom.headAtom.literal), new Clause(entry.getKey()))) { //todo check this method
                LinkedHashMap<WeightedRule, LinkedHashSet<WeightedRule>> ruleMap = support.computeIfAbsent(entry.getKey(), f -> new LinkedHashMap<>());

                for (Map.Entry<WeightedRule, LinkedHashSet<WeightedRule>> groundings : entry.getValue().entrySet()) {
                    LinkedHashSet<WeightedRule> weightedRules = ruleMap.computeIfAbsent(groundings.getKey(), f -> new LinkedHashSet<>());

                    for (WeightedRule grounding : groundings.getValue()) {
                        weightedRules.add(grounding);
                        recursePrune(grounding, support, new HashSet<>());
                    }
                }
            }
        }
        groundTemplate.groundRules = support;
        return this;
    }

    private void recursePrune(WeightedRule grounding, LinkedHashMap<Literal, LinkedHashMap<WeightedRule, LinkedHashSet<WeightedRule>>> support, Set<Literal> closedList) {
        for (BodyAtom bodyAtom : grounding.body) {
            if (closedList.contains(bodyAtom.literal)) {
                continue;
            }
            closedList.add(bodyAtom.literal);

            LinkedHashMap<WeightedRule, LinkedHashSet<WeightedRule>> validRules = groundRules.get(bodyAtom.literal);
            LinkedHashMap<WeightedRule, LinkedHashSet<WeightedRule>> nextRules = support.computeIfAbsent(bodyAtom.literal, f -> new LinkedHashMap<>());

            for (Map.Entry<WeightedRule, LinkedHashSet<WeightedRule>> validGroundings : validRules.entrySet()) {
                LinkedHashSet<WeightedRule> weightedRules = nextRules.computeIfAbsent(validGroundings.getKey(), f -> new LinkedHashSet<>());

                for (WeightedRule nextGrounding : validGroundings.getValue()) {
                    weightedRules.add(nextGrounding);
                    recursePrune(nextGrounding, support, closedList);
                }
            }
        }
    }

}
