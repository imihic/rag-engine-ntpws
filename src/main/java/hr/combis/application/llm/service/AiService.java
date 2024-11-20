package hr.combis.application.llm.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import hr.combis.application.data.model.Chat;
import hr.combis.application.data.model.SenderType;
import hr.combis.application.data.model.User;
import hr.combis.application.security.AuthenticatedUser;
import hr.combis.application.services.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Optional;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

@BrowserCallable
@AnonymousAllowed
public class AiService {

    private final hr.combis.application.service.ChatService chatService;
    private final hr.combis.application.service.MessageService messageService;
    private final AuthenticatedUser authenticatedUser;

    private Assistant assistant;
    private StreamingAssistant streamingAssistant;

    @Value("${openai.api.key}")
    private String OPENAI_API_KEY;

    public AiService(hr.combis.application.service.ChatService chatService, hr.combis.application.service.MessageService messageService, AuthenticatedUser authenticatedUser) {
        this.chatService = chatService;
        this.messageService = messageService;
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

        if (OPENAI_API_KEY == null) {
            System.err.println("ERROR: OPENAI_API_KEY environment variable is not set. Please set it to your OpenAI API key.");
        }

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

    public String chat(Long chatId, String userMessageContent) {
        Optional<User> user = authenticatedUser.get();

        Chat chat;
        if (chatId != null) {
            chat = chatService.getChat(chatId);
        } else {
            chat = chatService.createChat(user.get());
        }

        // Get AI response
        String aiResponse = assistant.chat(userMessageContent);

        // Save AI's message
        messageService.saveMessage(chat, SenderType.ASSISTANT, aiResponse);
        // Save user's message
        messageService.saveMessage(chat, SenderType.USER, userMessageContent);

        return aiResponse;
    }


    /*
    public Flux<String> chatStream(String message) {
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        streamingAssistant.chat(message)
                .onNext(sink::tryEmitNext)
                .onComplete(c -> sink.tryEmitComplete())
                .onError(sink::tryEmitError)
                .start();

        return sink.asFlux();
    }
    */
    public Flux<String> chatStream(Long chatId, String userMessageContent) {
        Optional<User> user = authenticatedUser.get();

        Chat chat;
        if (chatId != null) {
            chat = chatService.getChat(chatId);
        } else {
            chat = chatService.createChat(user.get());
        }

        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        StringBuilder aiResponseBuilder = new StringBuilder();

        streamingAssistant.chat(userMessageContent)
                .onNext(token -> {
                    aiResponseBuilder.append(token);
                    sink.tryEmitNext(token);
                })
                .onComplete(c -> {
                    messageService.saveMessage(chat, SenderType.USER, userMessageContent);
                    // Save AI's message after streaming completes
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