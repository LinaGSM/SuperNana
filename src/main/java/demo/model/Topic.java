package demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Comparator;
import java.util.stream.Collectors;

@Entity
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany
    @JoinTable(
            name = "message_topic", // Intermediate Table
            joinColumns = @JoinColumn(name = "topic_id"),
            inverseJoinColumns = @JoinColumn(name = "message_id")
    )
    @JsonBackReference // Prevents infinite recursion when serializing Messages
    private Set<Message> messages = new HashSet<>();

    public Topic() {}

    public Topic(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Message> getMessages() {
        return messages;
    }

    public void setMessages(Set<Message> messages) {
        this.messages = messages;
    }

    // Get messages in order by `indexInTopic`
    public List<Message> getOrderedMessages() {
        return messages.stream()
                .sorted(Comparator.comparingInt(Message::getIndexInTopic))
                .collect(Collectors.toList());
    }

    // Add a message and assign a unique index in the topic
    public void addMessage(Message message) {
        int nextIndex = messages.size() + 1; // Generates sequential index
        message.setIndexInTopic(nextIndex);
        this.messages.add(message);
    }

    //  Remove a message from the topic
    public void removeMessage(Message message) {
        this.messages.remove(message);
    }
}
