package demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String text;
    private boolean isRead = false;
    private int indexInTopic;

    @ManyToOne
    @JoinColumn(name = "queue_id")
    @JsonBackReference
    private Queue queue = null;

    @ManyToMany
    @JoinTable(
            name = "message_topic",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    @JsonBackReference
    private Set<Topic> associatedTopics = new HashSet<>();  // All the topic

    // message meta data
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm", timezone = "Europe/Paris") // Format how the date is displayed
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm", timezone = "Europe/Paris")
    private LocalDateTime firstAccessedAt;

    private int readCount = 0;


    // Constructors
    public Message() {}

    public Message(String text) {
        this.text = text;
        this.createdAt = LocalDateTime.now();
    }


    // getters
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getFirstAccessedAt() {
        return firstAccessedAt;
    }

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public boolean getIsRead() { return isRead; }

    public int getReadCount() { return readCount; }

    public Queue getQueue() {
        return queue;
    }

    public Set<Topic> getAssociatedTopics() { return associatedTopics; }

    public int getIndexInTopic() {
        return indexInTopic;
    }


    // setters
    public void setId(long id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    public void setAssociatedTopics(Set<Topic> topics) {
        this.associatedTopics = topics;
    }

    public void setIndexInTopic(int indexInTopic) {
        this.indexInTopic = indexInTopic;
    }


    // Methods
    public void markAsRead() {
        this.isRead = true;
        this.readCount++;
        if (this.firstAccessedAt == null) {
            this.firstAccessedAt = LocalDateTime.now();
        }
    }

}
