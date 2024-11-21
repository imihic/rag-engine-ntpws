package hr.combis.application.llm.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import hr.combis.application.data.model.Chat;
import hr.combis.application.data.model.DocumentSegment;
import hr.combis.application.data.model.SenderType;
import hr.combis.application.data.model.User;
import hr.combis.application.pipelines.util.models.BgeM3EmbeddingModel;
import hr.combis.application.security.AuthenticatedUser;
import hr.combis.application.services.ChatService;
import hr.combis.application.services.DocumentSegmentService;
import hr.combis.application.services.MessageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
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
    private final DocumentSegmentService documentSegmentService;
    private final AuthenticatedUser authenticatedUser;

    private Assistant assistant;
    private StreamingAssistant streamingAssistant;

    @Value("${openai.api.key}")
    private String OPENAI_API_KEY;

    public AiService(ChatService chatService, MessageService messageService, DocumentSegmentService documentSegmentService, AuthenticatedUser authenticatedUser) {
        this.chatService = chatService;
        this.messageService = messageService;
        this.documentSegmentService = documentSegmentService;
        this.authenticatedUser = authenticatedUser;
    }

    interface Assistant {
        String chat(String message);
    }

    interface StreamingAssistant {
        TokenStream chat(String message);
    }

    @PostConstruct
    public void init() {
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(OpenAiChatModel.withApiKey(OPENAI_API_KEY))
                .chatMemory(chatMemory)
                .build();

        streamingAssistant = AiServices.builder(StreamingAssistant.class)
                .streamingChatLanguageModel(OpenAiStreamingChatModel.withApiKey(OPENAI_API_KEY))
                .chatMemory(chatMemory)
                .build();
    }

    private String buildContext(User user, String userMessageContent) {
        // Generate embedding for the user's message
        float[] userMessageEmbedding = BgeM3EmbeddingModel.getInstance()
                .embed(userMessageContent)
                .content()
                .vector();

        // Perform similarity search
        List<DocumentSegment> similarSegments = documentSegmentService.findSimilarSegments(user, userMessageEmbedding, 5);

        // Build a context string from the retrieved segments
        StringBuilder contextBuilder = new StringBuilder("Relevant Context:\n");
        for (DocumentSegment segment : similarSegments) {
            contextBuilder.append(segment.getText()).append("\n---\n");
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

        Chat chat;
        if (chatId != null) {
            chat = chatService.getChat(chatId);
        } else {
            chat = chatService.createChat(user);
        }

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

        Chat chat;
        if (chatId != null) {
            chat = chatService.getChat(chatId);
        } else {
            chat = chatService.createChat(user);
        }

        // Build context
        String context = buildContext(user, userMessageContent);
        log.debug("Context: {}", context);

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
        Chat chat = chatService.createChat(user.get());
        return chat.getId();
    }
}
