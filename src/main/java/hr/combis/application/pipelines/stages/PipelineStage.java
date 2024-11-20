package hr.combis.application.pipelines.stages;

import hr.combis.application.pipelines.jobs.ProcessingJob;

/**
 * @author imihic
 */
public interface PipelineStage<T extends ProcessingJob> {
    T process(T input);

    Class<T> getInputType();  // This method should return the input type
}

