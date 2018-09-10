package constructs.building;

import constructs.example.LiftedExample;
import constructs.example.LogicSample;
import constructs.example.QueryAtom;
import constructs.example.ValuedFact;
import constructs.template.HeadAtom;
import ida.utils.tuples.Pair;
import learning.LearningSample;
import learning.Query;
import neuralogic.grammarParsing.PlainParseTree;
import org.antlr.v4.runtime.ParserRuleContext;
import settings.Settings;
import utils.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * //todo pridat query-to-multiple examples correspondence?? mozna...(spis ne)
 */
public abstract class SamplesBuilder<I extends PlainParseTree<? extends ParserRuleContext>, O> extends LogicSourceBuilder<I, Stream<O>> {
    private static final Logger LOG = Logger.getLogger(SamplesBuilder.class.getName());
    protected final Settings settings;

    final String prefix;
    int queryCounter = 0;

    public SamplesBuilder(Settings settings) {
        this.settings = settings;
        this.prefix = settings.sampleIdPrefix;
    }

    public Stream<LogicSample> buildSamplesFrom(I parseTree) {
        return buildFrom(parseTree).flatMap(this::sampleFrom);
    }

    protected abstract Stream<LogicSample> sampleFrom(O pair);


    /**
     * Stream TERMINATING OPERATION because of hashmap during merging!
     * Do not use separate (queries,examples) file if full streaming is desirable
     *
     * @param queries
     * @param examples
     * @return
     */
    public Stream<LogicSample> merge2streams(Stream<LogicSample> queries, Stream<LogicSample> examples) {
        if (settings.queriesAlignedWithExamples) {
            return Utilities.zipStreams(queries, examples, SamplesBuilder::merge2samples);
        }

        Map<String, Pair<LiftedExample, List<LogicSample>>> map;
        if (queries.isParallel() || examples.isParallel()) {
            if (settings.oneQueryPerExample)
                return Stream.concat(queries, examples).collect(Collectors.toConcurrentMap(LearningSample::getId, q -> q, SamplesBuilder::merge2samples)).values().stream();
            map = new ConcurrentHashMap<>();
        } else {
            if (settings.oneQueryPerExample)
                return Stream.concat(queries, examples).collect(Collectors.toMap(LearningSample::getId, q -> q, SamplesBuilder::merge2samples)).values().stream();
            map = new HashMap<>();
        }
        //the remaining 1 example to Many queries solution
        examples.forEach(ls -> map.put(ls.getId(), new Pair<>(ls.query.evidence, new ArrayList<>())));
        queries.forEach(ls -> {
            Pair<LiftedExample, List<LogicSample>> pair = map.get(ls.getId());
            ls.query.evidence = pair.r;
            List<LogicSample> qs = pair.s;
            qs.add(ls);
        });
        return map.values().stream().map(pair -> pair.s.stream()).flatMap(f -> f);
    }

    /**
     * Combined a separate query (wrapped in LogicSample) and example (wrapped in LogicSample) into a single LogicSample
     * Due to possible parallelism, it is not certain in what order they will come (i.e. q1,q2 are interchangebale)
     *
     * @param q1
     * @param q2
     * @return
     */
    private static LogicSample merge2samples(LogicSample q1, LogicSample q2) {
        LogicSample example = q1.query.evidence != null ? q1 : q2;
        LogicSample query = q1.target != null ? q1 : q2;

        if (query == example) {
            LOG.severe("Example-Query merging inconsistency: " + q1 + " + " + q2);
        }
        query.query.evidence = example.query.evidence;
        return query;
    }

    public QueryAtom createQueryAtom(String id, ValuedFact f, LiftedExample example) {
        return new QueryAtom(prefix + id, queryCounter++, settings.defaultSampleImportance, new HeadAtom(f.offsettedPredicate, f.literal.termList()), example);
    }

    public QueryAtom createQueryAtom(String id, double importance, ValuedFact f) {
        return new QueryAtom(prefix + id, queryCounter++, importance, new HeadAtom(f.offsettedPredicate, f.literal.termList()));
    }

    public QueryAtom createQueryAtom(String id, ValuedFact f) {
        return new QueryAtom(prefix + id, queryCounter++, settings.defaultSampleImportance, new HeadAtom(f.offsettedPredicate, f.literal.termList()));
    }
}