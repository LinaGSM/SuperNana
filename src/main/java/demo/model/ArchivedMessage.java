package demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ArchivedMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalContent;
    private LocalDateTime originalCreatedAt;
    private String originalQueueId;
    private LocalDateTime archivedAt;
    private int readCount;

    public ArchivedMessage() {}

    public ArchivedMessage(String content, LocalDateTime createdAt,
                           String queueId, int readCount) {
        this.originalContent = content;
        this.originalCreatedAt = createdAt;
        this.originalQueueId = queueId;
        this.readCount = readCount;
        this.archivedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public String getOriginalContent() { return originalContent; }
    public LocalDateTime getOriginalCreatedAt() { return originalCreatedAt; }
    public String getOriginalQueueId() { return originalQueueId; }
    public LocalDateTime getArchivedAt() { return archivedAt; }
    public int getReadCount() { return readCount; }

    // Setters
    public void setArchivedAt(LocalDateTime archivedAt) { this.archivedAt = archivedAt; }
}