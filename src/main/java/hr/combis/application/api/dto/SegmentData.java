package hr.combis.application.api.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Your SegmentData class
public class SegmentData {
    private String text;
    private Map<String, String> metadata;

    public SegmentData(String text, Map<String, String> metadata) {
        this.text = text;
        this.metadata = metadata;
    }

    // Getters and setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}