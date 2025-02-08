package demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Message {
    @Id
    @GeneratedValue
    private long id;
    private String text;

    @ManyToOne
    @JsonBackReference
    private MessageQueue queue;

    public Message() {
    }

    public Message(long id, String text) {
        this.id = id;
        this.text = text;
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
}
