package hr.combis.application.data.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Message extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "chat_id")
    @JsonBackReference // Add this annotation
    private Chat chat;

    @Enumerated(EnumType.STRING)
    private SenderType sender;

    @Lob
    @Column(length = 1000000)
    private String content;

    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public SenderType getSender() {
        return sender;
    }

    public void setSender(SenderType sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }   

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
