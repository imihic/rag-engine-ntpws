package hr.combis.application.pipelines;

import hr.combis.application.pipelines.jobs.ProcessingJob;
import hr.combis.application.pipelines.stages.PipelineStage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class PipelineExecutor<T extends ProcessingJob> {

    private final List<PipelineStage<T>> stages;

    public PipelineExecutor(List<PipelineStage<T>> stages) {
        this.stages = stages;
    }

    public T execute(T input) {
        T data = input;
        for (PipelineStage<T> stage : stages) {
            log.debug("Processing stage: {}", stage);
            data = stage.process(data);
        }
        return data;
    }
}
