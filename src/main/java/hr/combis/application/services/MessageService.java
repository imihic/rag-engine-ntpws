package hr.combis.application.services;

import hr.combis.application.data.model.Chat;
import hr.combis.application.data.model.Message;
import hr.combis.application.data.model.SenderType;
import hr.combis.application.data.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Transactional
    public Message saveMessage(Chat chat, SenderType sender, String content) {
        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setContent(content);
        return messageRepository.save(message);
    }

    public List<Message> getMessagesByChat(Chat chat) {
        return messageRepository.findByChatOrderByTimestampAsc(chat);
    }
}
