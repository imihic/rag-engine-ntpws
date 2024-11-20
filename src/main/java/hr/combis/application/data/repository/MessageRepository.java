package hr.combis.application.data.repository;

import hr.combis.application.data.model.Message;
import hr.combis.application.data.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChatOrderByTimestampAsc(Chat chat);
}
