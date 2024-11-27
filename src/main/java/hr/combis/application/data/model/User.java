package hr.combis.application.data.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Entity
@Table(name = "application_user")
@Setter
public class User extends AbstractEntity {

    private String username;
    private String name;

    @JsonIgnore
    private String hashedPassword;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> roles;

    @Lob
    @Column(length = 1000000)
    private byte[] profilePicture;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    @JsonManagedReference
    private Set<Chat> chats;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @JsonIgnore
    private List<DocumentEntity> documents;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
    @JsonManagedReference
    @Nullable
    private UserSettings userSettings;

    private boolean enabled;


}
