package hr.combis.application.services;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import hr.combis.application.api.dto.UserSettingsDto;
import hr.combis.application.data.model.User;
import hr.combis.application.security.AuthenticatedUser;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Endpoint
@AnonymousAllowed
@Slf4j
public class UserEndpoint {

    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticatedUser authenticatedUser;

    @Transactional
    public Optional<User> getAuthenticatedUser() {
        Optional<User> user = authenticatedUser.get();

        user.ifPresent(u -> {
            // Force initialization of lazy-loaded fields
            Hibernate.initialize(u.getChats());
            u.getChats().forEach(chat -> {
                Hibernate.initialize(chat.getMessages());
                // No need to initialize 'chat.getUser()' due to @JsonBackReference
            });
        });

        return user;
    }

    @Transactional
    public Optional<Long> getAuthenticatedUserId() {
        return authenticatedUser.get().map(User::getId);
    }

    @RolesAllowed("ADMIN")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @RolesAllowed("ADMIN")
    public void setUserAccess(String userId, boolean enabled) {
        userService.setUserAccess(userId, enabled);
    }


    @Transactional
    public UserSettingsDto getUserSettings() {
        Optional<User> userOpt = authenticatedUser.get();
        if (userOpt.isPresent()) {
            return userService.getUserSettings(userOpt.get().getId());
        } else {
            throw new RuntimeException("User not authenticated");
        }
    }

    @Transactional
    public void updateUserSettings(UserSettingsDto settingsDto) {
        Optional<User> userOpt = authenticatedUser.get();
        if (userOpt.isPresent()) {
            userService.updateUserSettings(userOpt.get().getId(), settingsDto);
        } else {
            throw new RuntimeException("User not authenticated");
        }
    }

    @Transactional
    public String getProfilePictureUrl() {
        Optional<User> userOpt = authenticatedUser.get();
        if (userOpt.isPresent()) {
            byte[] profilePicture = userOpt.get().getProfilePicture();
            if (profilePicture != null) {
                // Encode the image as a Data URL
                String base64Image = Base64.getEncoder().encodeToString(profilePicture);
                String mimeType = "image/png"; // Adjust based on actual image type
                return "data:" + mimeType + ";base64," + base64Image;
            }
        }
        return null;
    }
}
