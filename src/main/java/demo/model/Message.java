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
    private int indexInTopic; // ✅ Internal numbering within a topic

    @ManyToOne
    @JsonBackReference // ✅ Prevents infinite recursion with MessageQueue
    private MessageQueue queue;

    @ManyToMany
    @JoinTable(
            name = "message_topic",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    @JsonBackReference // ✅ Prevents infinite recursion
    private Set<Topic> topics = new HashSet<>();

    // ✅ Meta-data fields
    private LocalDateTime createdAt;      // Time message was created
    private LocalDateTime firstAccessedAt;// Time message was first read
    private int readCount = 0;            // Number of times message was read

    public Message() {
        this.createdAt = LocalDateTime.now(); // ✅ Auto-assign creation time
    }


    public Message(String text) {
        this.text = text;
        this.createdAt = LocalDateTime.now();
    }

    // ✅ Update metadata when message is read
    public void markAsRead(boolean b) {
        this.isRead = true;
        this.readCount++;
        if (this.firstAccessedAt == null) {
            this.firstAccessedAt = LocalDateTime.now();
        }
    }

    // ✅ Getters and setters
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

    // ✅ Getter & Setter for ID
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    // ✅ Getter & Setter for Text
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    // ✅ Getter & Setter for Queue
    public MessageQueue getQueue() {
        return queue;
    }

    public void setQueue(MessageQueue queue) {
        this.queue = queue;
    }

    // ✅ Getter & Setter for Read Status
    public boolean isRead() {
        return isRead;
    }

    public void markAsRead() {
        this.isRead = true;
    }

    // ✅ Getter & Setter for `indexInTopic`
    public int getIndexInTopic() {
        return indexInTopic;
    }

    public void setIndexInTopic(int indexInTopic) {
        this.indexInTopic = indexInTopic;
    }

    // ✅ Getter & Setter for Topics
    public Set<Topic> getTopics() {
        return topics;
    }

    public void setTopics(Set<Topic> topics) {
        this.topics = topics;
    }

    // ✅ Helper Methods for Managing Topics
    public void addTopic(Topic topic) {
        this.topics.add(topic);
        topic.getMessages().add(this);
        this.indexInTopic = topic.getMessages().size(); // ✅ Assigns sequential index
    }

    public void removeTopic(Topic topic) {
        this.topics.remove(topic);
        topic.getMessages().remove(this);
    }
}
