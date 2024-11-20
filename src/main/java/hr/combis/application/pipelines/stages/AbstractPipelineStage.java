package hr.combis.application.pipelines.stages;

import hr.combis.application.pipelines.jobs.ProcessingJob;

public abstract class AbstractPipelineStage<T extends ProcessingJob> implements PipelineStage<T> {

    private final Class<T> inputType;

    protected AbstractPipelineStage(Class<T> inputType) {
        this.inputType = inputType;
    }

    @Override
    public Class<T> getInputType() {
        return inputType;
    }

}
