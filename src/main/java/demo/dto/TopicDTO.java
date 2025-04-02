package demo.dto;

import demo.model.Message;
import java.util.List;

public class TopicDTO {
    private Long topicId;
    private String name;
    private List<Message> messages;


    public TopicDTO(Long topicId, String name, List<Message> messages) {
        this.topicId = topicId;
        this.name = name;
        this.messages = messages;
    }

    public Long getTopicId() {
        return topicId;
    }

    public String getName() {
        return name;
    }

    public List<Message> getMessages() {
        return messages;
    }
}
