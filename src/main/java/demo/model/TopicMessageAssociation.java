package demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "topic_message_association")
public class TopicMessageAssociation {

    @EmbeddedId
    private TopicMessageKey id;

    @ManyToOne
    @JsonBackReference
    @MapsId("topicId")
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @ManyToOne
    @JsonBackReference
    @MapsId("messageId")
    @JoinColumn(name = "message_id")
    private Message message;

    private int positionIndex;

    // Constructors
    public TopicMessageAssociation() {
        this.id = new TopicMessageKey();
    }

    public TopicMessageAssociation(Topic topic, Message message, int positionIndex) {
        this.id = new TopicMessageKey(topic.getId(), message.getId());
        this.topic = topic;
        this.message = message;
        this.positionIndex = positionIndex;
    }

    // Getters
    public TopicMessageKey getId() { return id; }
    public Message getMessage() { return message; }
    public Topic getTopic() { return topic; }
    public int getPositionIndex() { return positionIndex; }

    // Setters
    public void setId(TopicMessageKey id) { this.id = id; }
    public void setMessage(Message message) {
        this.message = message;
        this.id.setMessageId(message.getId());
    }
    public void setTopic(Topic topic) {
        this.topic = topic;
        this.id.setTopicId(topic.getId());
    }
    public void setPositionIndex(int positionIndex) { this.positionIndex = positionIndex; }
}