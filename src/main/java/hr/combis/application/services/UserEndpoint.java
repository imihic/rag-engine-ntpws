package hr.combis.application.services;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import hr.combis.application.data.model.User;
import hr.combis.application.security.AuthenticatedUser;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@Endpoint
@AnonymousAllowed
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

}
