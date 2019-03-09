package constructs.template.components;

import constructs.template.metadata.RuleMetadata;
import ida.ilp.logic.Clause;
import ida.ilp.logic.HornClause;
import ida.ilp.logic.Literal;
import ida.ilp.logic.Term;
import networks.computation.evaluation.functions.Activation;
import networks.structure.components.weights.Weight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Gusta on 04.10.2016.
 * <p>
 *
 */
public class WeightedRule {

    /**
     * changable by structure learning?
     */
    boolean isEditable = false;

    public Weight weight;
    public Weight offset;

    public HeadAtom head;
    public List<BodyAtom> body;

    public Activation aggregationFcn;
    public Activation activationFcn;

    public RuleMetadata metadata;
    public String originalString;

    /**
     * Apply {@link networks.computation.evaluation.functions.CrossProduct} activation on the inputs of the rule?
     */
    public boolean crossProduct;

    public WeightedRule() {

    }

    /**
     * This does not really clone the rule, only references
     * @param other
     */
    public WeightedRule(WeightedRule other) {
        this.weight = other.weight;
        this.head = other.head;
        this.body = new ArrayList<>(other.body.size());
        this.body.addAll(other.body);
        this.offset = other.offset;
        this.aggregationFcn = other.aggregationFcn;
        this.activationFcn = other.activationFcn;
        this.metadata = other.metadata;
        this.originalString = other.originalString;
        this.isEditable = other.isEditable;
    }

    public HornClause toHornClause() {
        List<Literal> collected = body.stream().map(bodyLit -> bodyLit.getLiteral()).collect(Collectors.toList());
        return new HornClause(head.getLiteral(), new Clause(collected));
    }

    /**
     * Grounding of individual atoms will create new copies of them.
     * @param variables
     * @param terms
     * @return
     */
    public WeightedRule ground(Term[] variables, Term[] terms) {
        WeightedRule ruleCopy = new WeightedRule(this);

        Map<Term, Term> var2term = new HashMap<Term, Term>();
        for (int i = 0; i < variables.length; i++) {
            var2term.put(variables[i], terms[i]);
        }

        ruleCopy.head = head.ground(var2term);
        for (int i = 0; i < body.size(); i++) {
            ruleCopy.body.set(i, body.get(i).ground(var2term));
        }

        return ruleCopy;
    }

    public String signatureString() {
        StringBuilder sb = new StringBuilder();
        sb.append(head.getPredicate()).append(":-");
        for (BodyAtom bodyAtom : body) {
            sb.append(bodyAtom.getPredicate()).append(",");
        }
        return sb.toString();
    }

    public boolean hasWeightedBody() {
        for (BodyAtom bodyAtom : body) {
            if (bodyAtom.weight != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * todo test go back to default hash?
     * @return
     */
    @Override
    public int hashCode() {
        return head.hashCode() + body.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WeightedRule)) {
            return false;
        }
        WeightedRule other = (WeightedRule) obj;
        if (!weight.equals(other.weight) || !offset.equals(other.offset)) {
            return false;
        }
        if (!aggregationFcn.equals(other.aggregationFcn) || ! activationFcn.equals(other.activationFcn)){
            return false;
        }
        if (!head.equals(other.head) || ! body.equals(other.body)){
            return false;
        }
        return true;
    }

    public String toShortString() {
        StringBuilder sb = new StringBuilder();
        sb.append(head.toString()).append(":-");
        for (BodyAtom bodyAtom : body) {
            sb.append(bodyAtom.toString()).append(",");
        }
        sb.setCharAt(sb.length()-1,'.');
        return sb.toString();
    }
}