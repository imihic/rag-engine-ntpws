package hr.combis.application.services;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import hr.combis.application.data.model.*;
import hr.combis.application.pipelines.util.models.BgeM3EmbeddingModel;
import hr.combis.application.security.AuthenticatedUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.Optional;

@BrowserCallable
@AnonymousAllowed
@Slf4j
public class AiService {

    private final ChatService chatService;
    private final MessageService messageService;
    private final ChunkService chunkService;
    private final AuthenticatedUser authenticatedUser;

    @Autowired
    private UserService userService;

    @Value("${openai.api.key}")
    private String OPENAI_API_KEY;

    public AiService(ChatService chatService, MessageService messageService, ChunkService chunkService, AuthenticatedUser authenticatedUser) {
        this.chatService = chatService;
        this.messageService = messageService;
        this.chunkService = chunkService;
        this.authenticatedUser = authenticatedUser;
    }

    interface Assistant {
        String chat(String message);
    }

    interface StreamingAssistant {
        TokenStream chat(String message);
    }

    private Assistant createAssistant(User user) {
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        String apiKey = OPENAI_API_KEY; // Your API Key

        UserSettings settings = user.getUserSettings();
        String model = settings != null && settings.getOpenAiModel() != null ? settings.getOpenAiModel() : "gpt-3.5-turbo";
        Double temperature = settings != null && settings.getTemperature() != null ? settings.getTemperature() : 0.7;

        return AiServices.builder(Assistant.class)
                .chatLanguageModel(OpenAiChatModel.builder()
                        .apiKey(apiKey)
                        .modelName(model)
                        .temperature(temperature)
                        .build())
                .chatMemory(chatMemory)
                .build();
    }

    private StreamingAssistant createStreamingAssistant(User user) {
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        String apiKey = OPENAI_API_KEY; // Your API Key

        UserSettings settings = user.getUserSettings();
        String model = settings != null && settings.getOpenAiModel() != null ? settings.getOpenAiModel() : "gpt-3.5-turbo";
        Double temperature = settings != null && settings.getTemperature() != null ? settings.getTemperature() : 0.7;

        return AiServices.builder(StreamingAssistant.class)
                .streamingChatLanguageModel(OpenAiStreamingChatModel.builder()
                        .apiKey(apiKey)
                        .modelName(model)
                        .temperature(temperature)
                        .build())
                .chatMemory(chatMemory)
                .build();
    }

    private String buildContext(User user, String userMessageContent) {
        // Generate embedding for the user's message
        float[] userMessageEmbedding = BgeM3EmbeddingModel.getInstance()
                .embed(userMessageContent)
                .content()
                .vector();

        // Perform similarity search using ChunkService
        List<Chunk> similarChunks = chunkService.findSimilarChunks(user, userMessageEmbedding, 2);

        // Build a context string from the retrieved chunks
        StringBuilder contextBuilder = new StringBuilder("Relevant Context:\n");
        for (Chunk chunk : similarChunks) {
            contextBuilder.append(chunk.getText()).append("\n---\n");
        }
        log.debug("Context: {}", contextBuilder.toString());
        return contextBuilder.toString();
    }

    public String chat(Long chatId, String userMessageContent) {
        Optional<User> userOpt = authenticatedUser.get();
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not authenticated");
        }

        User user = userOpt.get();

        Assistant assistant = createAssistant(user);

        Chat chat;
        chat = chatService.getChat(chatId);

        // Build context
        String context = buildContext(user, userMessageContent);

        // Get AI response
        String aiResponse = assistant.chat(context + "\nUser Message:\n" + userMessageContent);

        // Save messages
        messageService.saveMessage(chat, SenderType.USER, userMessageContent);
        messageService.saveMessage(chat, SenderType.ASSISTANT, aiResponse);

        return aiResponse;
    }

    public Flux<String> chatStream(Long chatId, String userMessageContent) {
        Optional<User> userOpt = authenticatedUser.get();
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not authenticated");
        }

        User user = userOpt.get();

        StreamingAssistant streamingAssistant = createStreamingAssistant(user);

        Chat chat;
        chat = chatService.getChat(chatId);

        // Build context
        String context = buildContext(user, userMessageContent);
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();
        StringBuilder aiResponseBuilder = new StringBuilder();

        streamingAssistant.chat(context + "\nUser Message:\n" + userMessageContent)
                .onNext(token -> {
                    aiResponseBuilder.append(token);
                    sink.tryEmitNext(token);
                })
                .onComplete(c -> {
                    messageService.saveMessage(chat, SenderType.USER, userMessageContent);
                    messageService.saveMessage(chat, SenderType.ASSISTANT, aiResponseBuilder.toString());
                    sink.tryEmitComplete();
                })
                .onError(sink::tryEmitError)
                .start();

        return sink.asFlux();
    }

    public Long createChat() {
        Optional<User> user = authenticatedUser.get();
        if (user.isEmpty()) {
            throw new RuntimeException("User not authenticated");
        }
        Chat chat = chatService.createChat(user.get());
        return chat.getId();
    }
}
