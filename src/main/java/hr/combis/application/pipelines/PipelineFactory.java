package hr.combis.application.pipelines;

import hr.combis.application.pipelines.config.PipelineConfig;
import hr.combis.application.pipelines.jobs.ProcessingJob;
import hr.combis.application.pipelines.stages.PipelineStage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class PipelineFactory {

    private final ApplicationContext applicationContext;
    private final PipelineConfig pipelineConfig;

    public PipelineFactory(ApplicationContext applicationContext, PipelineConfig pipelineConfig) {
        this.applicationContext = applicationContext;
        this.pipelineConfig = pipelineConfig;
    }

    public <T extends ProcessingJob> PipelineExecutor<T> createPipelineExecutor(String pipelineType) {
        PipelineConfig.PipelineDefinition definition = pipelineConfig.getPipelineDefinitions().get(pipelineType);

        if (definition == null) {
            log.error("No pipeline configuration found for type: {}", pipelineType);
            throw new IllegalArgumentException("No pipeline configuration found for type: " + pipelineType);
        }

        String jobTypeClassName = definition.getJobType();
        Class<T> jobClass;
        try {
            jobClass = (Class<T>) Class.forName(jobTypeClassName);
        } catch (ClassNotFoundException e) {
            log.error("Invalid job type class: {}", jobTypeClassName, e);
            throw new IllegalArgumentException("Invalid job type class: " + jobTypeClassName, e);
        }

        // Collect the pipeline stages
        List<PipelineStage<T>> stages = new ArrayList<>();
        for (String stageName : definition.getStages()) {
            PipelineStage<?> rawStage = (PipelineStage<?>) applicationContext.getBean(stageName);

            // Use a safe cast to PipelineStage<T> if the stage matches the expected types
            if (rawStage.getInputType().isAssignableFrom(jobClass)) {
                stages.add((PipelineStage<T>) rawStage);
            } else {
                log.error("Pipeline stage {} is not compatible with job type {}", stageName, jobTypeClassName);
                throw new IllegalArgumentException("Pipeline stage " + stageName + " is not compatible with job type " + jobTypeClassName);
            }
        }
        // Return the correctly typed PipelineExecutor
        return new PipelineExecutor<>(stages);
    }
}
