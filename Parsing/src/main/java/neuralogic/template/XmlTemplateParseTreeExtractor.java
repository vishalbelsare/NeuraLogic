package neuralogic.template;

import constructs.Conjunction;
import constructs.example.ValuedFact;
import constructs.WeightedPredicate;
import constructs.template.components.WeightedRule;
import ida.utils.tuples.Pair;
import networks.structure.components.weights.Weight;
import neuralogic.grammarParsing.GrammarVisitor;
import parsing.antlr.NeuralogicParser;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Deprecated
public class XmlTemplateParseTreeExtractor extends TemplateParseTreeExtractor {
    private static final Logger LOG = Logger.getLogger(XmlTemplateParseTreeExtractor.class.getName());

    XmlTemplateParseTreeExtractor(GrammarVisitor v) {
        super(v);
    }


    @Override
    public List<WeightedRule> getWeightedRules(NeuralogicParser.TemplateFileContext ctx) {
        return null;
    }

    @Override
    public List<ValuedFact> getWeightedFacts(NeuralogicParser.TemplateFileContext ctx) {
        return null;
    }

    @Override
    public List<Conjunction> getWeightedConjunctions(NeuralogicParser.TemplateFileContext ctx) {
        return null;
    }

    @Override
    public List<Pair<Weight, Map<String, Object>>> getWeightsMetadata(NeuralogicParser.TemplateFileContext ctx) {
        return null;
    }

    @Override
    public List<Pair<WeightedPredicate, Map<String, Object>>> getPredicatesMetadata(NeuralogicParser.TemplateFileContext ctx) {
        return null;
    }

    @Override
    public Map<String, Object> getTemplateMetadata(NeuralogicParser.TemplateFileContext ctx) {
        return null;
    }
}
