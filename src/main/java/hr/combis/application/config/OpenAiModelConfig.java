package hr.combis.application.config;

import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiModelConfig {

    @Value("${openai.api.key}")
    private String OPENAI_API_KEY;

    @Bean
    public OpenAiChatModel openAiModel() {
        return OpenAiChatModel.builder()
                .apiKey(OPENAI_API_KEY)
                .modelName("gpt-4o-mini")
                .temperature(0.1)
                .build();
    }


    @Bean
    public OpenAiChatModel tritonModel() {
        return OpenAiChatModel.builder()
                .apiKey("YOUR_API_KEY_FOR_MODEL_TWO")
                .baseUrl("https://endpoint-for-model-two")
                .modelName("gpt-4")
                .temperature(0.5)
                .build();
    }

}
