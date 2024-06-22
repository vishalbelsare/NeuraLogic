package cz.cvut.fel.ida.pipelines.pipes.specific;

import cz.cvut.fel.ida.logic.constructs.example.LogicSample;
import cz.cvut.fel.ida.logic.constructs.template.Template;
import cz.cvut.fel.ida.logic.grounding.GroundingSample;
import cz.cvut.fel.ida.pipelines.Pipe;
import cz.cvut.fel.ida.setup.Settings;
import cz.cvut.fel.ida.utils.generic.Pair;

import java.util.logging.Logger;
import java.util.stream.Stream;

public class GroundingSampleWrappingPipe extends Pipe<Pair<Template, Stream<LogicSample>>, Stream<GroundingSample>> {
    private static final Logger LOG = Logger.getLogger(GroundingSampleWrappingPipe.class.getName());
    private final Settings settings;

    public GroundingSampleWrappingPipe(Settings settings) {
        super("GroundingSampleWrappingPipe");
        this.settings = settings;
    }

    @Override
    public Stream<GroundingSample> apply(Pair<Template, Stream<LogicSample>> templateStreamPair) {
        if (templateStreamPair.s.isParallel()) {
            LOG.warning("Samples come in parallel into grounder already, this may have negative effect on shared grounding."); //can be: https://stackoverflow.com/questions/29216588/how-to-ensure-order-of-processing-in-java8-streams
            if (!settings.oneQueryPerExample)
                templateStreamPair.s = templateStreamPair.s.sequential();
        }
        final GroundingSample.Wrap lastGroundingWrap = new GroundingSample.Wrap(null);
        Stream<GroundingSample> groundingSampleStream = templateStreamPair.s.map(sample -> {
            if (sample.query.evidence == null) {
                String err = "Query-Example mismatch: No example evidence was matched for this query: #" + sample.query.position + ":" + sample.query;
                LOG.severe(err);
                throw new RuntimeException(err);
            }
            GroundingSample groundingSample = new GroundingSample(sample, templateStreamPair.r);
//            if (settings.groundingMode == Settings.GroundingMode.GLOBAL || settings.groundingMode == Settings.GroundingMode.SEQUENTIAL || lastGroundingWrap.getExample() == null) {
            groundingSample.groundingWrap = lastGroundingWrap;
//            }
            if (sample.query.evidence.equals(lastGroundingWrap.getExample())) {
//                groundingSample.groundingWrap = lastGroundingWrap;
                groundingSample.groundingComplete = true;
            } else {
                lastGroundingWrap.setExample(sample.query.evidence);    // setup the new example
                lastGroundingWrap.setNeuronMaps(null);  // remove the old neurons - they will need to be built again
                groundingSample.groundingComplete = false;
            }
            return groundingSample;
        });
        return groundingSampleStream;
    }
}
