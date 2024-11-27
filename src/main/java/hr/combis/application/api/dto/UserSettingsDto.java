package hr.combis.application.api.dto;

import hr.combis.application.data.model.UserSettings;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSettingsDto {

    private String openAiModel;
    private Double temperature;
    private Boolean darkModeEnabled;
    private String preferredLocale;

    public UserSettingsDto() {
        // Set default values
        this.openAiModel = "gpt-3.5-turbo";
        this.temperature = 0.7;
        this.darkModeEnabled = true; // Enable dark mode by default
        this.preferredLocale = "en"; // English by default
    }

    public UserSettingsDto(UserSettings settings) {
        this.openAiModel = settings.getOpenAiModel() != null ? settings.getOpenAiModel() : "gpt-3.5-turbo";
        this.temperature = settings.getTemperature() != null ? settings.getTemperature() : 0.7;
        this.darkModeEnabled = settings.getDarkModeEnabled() != null ? settings.getDarkModeEnabled() : true;
        this.preferredLocale = settings.getPreferredLocale() != null ? settings.getPreferredLocale() : "en";
    }
}
