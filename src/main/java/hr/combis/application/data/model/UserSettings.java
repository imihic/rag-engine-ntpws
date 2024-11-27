package hr.combis.application.data.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
public class UserSettings extends AbstractEntity {

    @Column(name = "openai_model")
    private String openAiModel;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "dark_mode_enabled")
    private Boolean darkModeEnabled;

    @Column(name = "preferred_locale")
    private String preferredLocale;

    // One-to-One relationship with User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;
}