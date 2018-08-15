package learning;

import networks.evaluation.values.Value;
import networks.structure.Weight;

import java.util.List;

public interface Model<T extends Query>  {
    String getId();

    Value evaluate(T query);
    List<Weight> getAllWeights();
}