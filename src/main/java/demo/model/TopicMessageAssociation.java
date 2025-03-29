package demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "topic_message_associations")
public class TopicMessageAssociation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @ManyToOne
    @JoinColumn(name = "message_id")
    private Message message;

    private int positionIndex;

    // Constructors
    public TopicMessageAssociation() {}

    public TopicMessageAssociation(Topic topic, Message message, int positionIndex) {
        this.topic = topic;
        this.message = message;
        this.positionIndex = positionIndex;
    }

    // Getters
    public Long getId() { return id; }

    public Message getMessage() { return message; }

    public Topic getTopic() { return topic; }

    public int getPositionIndex() { return positionIndex; }


    // Setters
    public void setId(Long id) { this.id = id; }

    public void setMessage(Message message) { this.message = message; }

    public void setTopic(Topic topic) { this.topic = topic; }

    public void setPositionIndex(int positionIndex) { this.positionIndex = positionIndex; }



}
