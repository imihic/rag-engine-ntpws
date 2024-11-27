package hr.combis.application.services;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import hr.combis.application.data.model.Chat;
import hr.combis.application.data.model.Message;
import hr.combis.application.data.model.SenderType;
import hr.combis.application.data.model.User;
import hr.combis.application.security.AuthenticatedUser;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@Endpoint
@AnonymousAllowed
@Slf4j
public class ChatEndpoint {

    @Autowired
    private AuthenticatedUser authenticatedUser;

    @Autowired
    private ChatService chatService;

    @Autowired
    private MessageService messageService;

    /**
     * Retrieves a list of chats started by the authenticated user.
     *
     * @return List of chats.
     */
    @Transactional
    public List<Chat> getUserChats() {
        Optional<User> user = authenticatedUser.get();
        if (user.isEmpty()) {
            throw new RuntimeException("Authenticated user not found");
        }

        List<Chat> chats = chatService.getChatsByUser(user.get());
        // Initialize lazy-loaded fields
        chats.forEach(chat -> chat.getMessages().size());
        return chats;
    }

    /**
     * Retrieves messages of a specific chat.
     *
     * @param chatId ID of the chat.
     * @return List of messages.
     */
    @Transactional
    public List<Message> getChatMessages(Long chatId) {
        Chat chat = chatService.getChat(chatId);
        return messageService.getMessagesByChat(chat);
    }


    @Transactional
    public void saveMessage(Long chatId, String senderType, String content) {
        Chat chat = chatService.getChat(chatId);
        messageService.saveMessage(chat, SenderType.valueOf(senderType), content);
    }
}
