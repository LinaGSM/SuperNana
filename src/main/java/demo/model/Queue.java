package demo.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Queue {
    @Id
    private String id;

    @OneToMany(mappedBy = "queue", cascade = CascadeType.ALL) // Fixed "topic" to "queue"
    @JsonManagedReference
    private List<Message> messages;

    // Constructors
    public Queue() {
        this.messages = new LinkedList<>();
    }

    public Queue(String id) {
        this.id = id;
        this.messages = new LinkedList<>();
    }


    // Getters
    public String getId() {
        return id;
    }

    public List<Message> getMessages() {
        return messages;
    }


    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }


    //Methods

    public void addMessage(Message message) {
        this.messages.add(message);
        message.setQueue(this);
    }

    public void removeMessage(Message message) {
        Optional<Message> msg = messages.stream().filter(x -> x.getId() == message.getId()).findFirst();
        msg.ifPresent(m -> {
            messages.remove(m);
            m.setQueue(null);
        });
    }




}
