package hr.combis.application.pipelines.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "pipelines")
@Getter
@Setter
@Slf4j
public class PipelineConfig {

    private Map<String, PipelineDefinition> pipelineDefinitions;

    @PostConstruct
    public void init() {
        if (pipelineDefinitions == null || pipelineDefinitions.isEmpty()) {
            log.error("Pipelines are not loaded from application.yml!");
        } else {
            log.info("Pipelines loaded: {}", pipelineDefinitions);
        }
    }

    @Getter
    @Setter
    public static class PipelineDefinition {
        private String jobType;
        private List<String> stages;
    }
}

