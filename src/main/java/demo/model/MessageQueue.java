package demo.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class MessageQueue {
    @Id
    private String id;

    @OneToMany
    @JsonManagedReference
    private List<Message> messages;

    public MessageQueue() {
        this.messages = new LinkedList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
        message.setQueue(this);
    }

    public Message nextMessage() {
        return this.messages.get(0);
    }

    public Optional<Message> removeMessage(long mid) {
        Optional<Message> r = messages.stream().filter(x -> x.getId() == mid).findFirst();
        r.ifPresent(m -> {
            messages.remove(m);
            m.setQueue(null);
        });
        return r;
    }
}
