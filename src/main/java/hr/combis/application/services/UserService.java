package hr.combis.application.services;

import hr.combis.application.api.dto.UserSettingsDto;
import hr.combis.application.data.model.User;
import hr.combis.application.data.model.UserSettings;
import hr.combis.application.data.repository.UserRepository;
import hr.combis.application.data.repository.UserSettingsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private final UserRepository repository;
    private final UserSettingsRepository userSettingsRepository;

    public UserService(UserRepository repository, UserSettingsRepository userSettingsRepository) {
        this.userSettingsRepository = userSettingsRepository;
        this.repository = repository;
    }

    public Optional<User> get(Long id) {
        return repository.findById(id);
    }

    public User update(User entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<User> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<User> list(Pageable pageable, Specification<User> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    public Optional<User> findById(Long id) {
        return repository.findById(id);
    }

    public List<User> getAllUsers() {
        return repository.findAll();
    }

    public void setUserAccess(String userId, boolean enabled) {
        User user = repository.findById(Long.valueOf(userId)).orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(enabled);
        repository.save(user);
    }

    public void updateUserSettings(Long userId, UserSettingsDto settingsDto) {
        Optional<User> userOpt = repository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            UserSettings settings = user.getUserSettings();
            if (settings == null) {
                settings = new UserSettings();
                settings.setUser(user);
                user.setUserSettings(settings);
            }

            // Update settings from DTO
            settings.setOpenAiModel(settingsDto.getOpenAiModel());
            settings.setTemperature(settingsDto.getTemperature());
            settings.setDarkModeEnabled(settingsDto.getDarkModeEnabled());
            settings.setPreferredLocale(settingsDto.getPreferredLocale());

            userSettingsRepository.save(settings);
            repository.save(user);
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public UserSettingsDto getUserSettings(Long userId) {
        Optional<User> userOpt = repository.findById(userId);
        if (userOpt.isPresent()) {
            UserSettings settings = userOpt.get().getUserSettings();
            if (settings != null) {
                return new UserSettingsDto(settings);
            } else {
                // Return default settings
                return new UserSettingsDto();
            }
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public void updateProfilePicture(Long userId, byte[] profilePicture) {
        Optional<User> userOpt = repository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setProfilePicture(profilePicture);
            repository.save(user);
        } else {
            throw new RuntimeException("User not found");
        }
    }

}
