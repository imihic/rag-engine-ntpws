package hr.combis.application.service;

import hr.combis.application.data.model.Chat;
import hr.combis.application.data.model.User;
import hr.combis.application.data.repository.ChatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ChatService {

    private final ChatRepository chatRepository;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Transactional
    public Chat createChat(User user) {
        Chat chat = new Chat();
        chat.setUser(user);
        return chatRepository.save(chat);
    }

    public Chat getChat(Long chatId) {
        return chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
    }
}