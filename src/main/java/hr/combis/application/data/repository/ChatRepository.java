package hr.combis.application.data.repository;

import hr.combis.application.data.model.Chat;
import hr.combis.application.data.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByUser(User user);
}
