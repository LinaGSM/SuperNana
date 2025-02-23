package demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
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
    @JsonBackReference
    private MessageQueue queue;

    @ManyToMany
    @JoinTable(
            name = "message_topic",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    @JsonBackReference
    private Set<Topic> topics = new HashSet<>();

    private LocalDateTime createdAt;
    private LocalDateTime firstAccessedAt;
    private int readCount = 0;

    public Message() {
        this.createdAt = LocalDateTime.now();
    }

    public Message(String text) {
        this.text = text;
        this.createdAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getFirstAccessedAt() {
        return firstAccessedAt;
    }

    public int getReadCount() {
        return readCount;
    }

    public Message(String text, MessageQueue queue) {
        this.text = text;
        this.queue = queue;
    }

    public Message(String text, MessageQueue queue, Set<Topic> topics) {
        this.text = text;
        this.queue = queue;
        this.topics = topics;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public MessageQueue getQueue() {
        return queue;
    }

    public void setQueue(MessageQueue queue) {
        this.queue = queue;
    }

    public boolean isRead() {
        return isRead;
    }

    public void markAsRead() {
        this.isRead = true;
        this.readCount++;
        if (this.firstAccessedAt == null) {
            this.firstAccessedAt = LocalDateTime.now();
        }
    }

    public int getIndexInTopic() {
        return indexInTopic;
    }

    public void setIndexInTopic(int indexInTopic) {
        this.indexInTopic = indexInTopic;
    }

    public Set<Topic> getTopics() {
        return topics;
    }

    public void setTopics(Set<Topic> topics) {
        this.topics = topics;
    }

    public void addTopic(Topic topic) {
        this.topics.add(topic);
        topic.getMessages().add(this);
        this.indexInTopic = topic.getMessages().size();
    }

    public void removeTopic(Topic topic) {
        this.topics.remove(topic);
        topic.getMessages().remove(this);
    }
}
