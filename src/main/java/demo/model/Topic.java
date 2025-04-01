package demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private Set<TopicMessageAssociation> messageAssociations = new HashSet<>();


    // Constructors
    public Topic() {}

    public Topic(String name) {
        this.name = name;
    }


    // Getters
    public Long getId() {return id; }

    public String getName() {
        return name;
    }

    public Set<TopicMessageAssociation>  getMessageAssociations() {
        return messageAssociations;
    }


    // Setters
    public void setId(Long id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    public void setMessages(Set<TopicMessageAssociation>  messageAssociations) {
        this.messageAssociations = messageAssociations;
    }


}
