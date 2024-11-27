package hr.combis.application.data.repository;

import hr.combis.application.data.model.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
    // Additional query methods if needed
}
