package demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TopicMessageKey implements Serializable {

    @Column(name = "topic_id")
    private Long topicId;

    @Column(name = "message_id")
    private Long messageId;

    // Constructors
    public TopicMessageKey() {}

    public TopicMessageKey(Long topicId, Long messageId) {
        this.topicId = topicId;
        this.messageId = messageId;
    }

    // Getters et setters
    public Long getTopicId() { return topicId; }
    public Long getMessageId() { return messageId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopicMessageKey that = (TopicMessageKey) o;
        return Objects.equals(topicId, that.topicId) &&
                Objects.equals(messageId, that.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicId, messageId);
    }
}