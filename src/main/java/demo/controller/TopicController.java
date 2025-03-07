package demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import demo.model.Message;
import demo.model.Topic;
import demo.controller.MessageRepository;
import demo.controller.TopicRepository;

import java.util.Optional;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/topics")
public class TopicController {

    @Autowired
    private TopicRepository topicRepo;

    @Autowired
    private MessageRepository messageRepo;

    // Create a new topic
    @PostMapping
    public ResponseEntity<Topic> createTopic(@RequestBody String name) {
        Topic topic = new Topic(name);
        topicRepo.save(topic);
        return new ResponseEntity<>(topic, HttpStatus.CREATED);
    }

    // Add a message to a topic & assign indexInTopic
    @PostMapping("/{topicId}/messages/{messageId}")
    public ResponseEntity<Topic> addMessageToTopic(
            @PathVariable Long topicId, @PathVariable Long messageId) {

        Optional<Topic> topicOpt = topicRepo.findById(topicId);
        Optional<Message> messageOpt = messageRepo.findById(messageId);

        if (topicOpt.isPresent() && messageOpt.isPresent()) {
            Topic topic = topicOpt.get();
            Message message = messageOpt.get();

            // Assign sequential `indexInTopic`
            message.setIndexInTopic(topic.getMessages().size() + 1);

            topic.addMessage(message);
            topicRepo.save(topic);
            messageRepo.save(message);

            return new ResponseEntity<>(topic, HttpStatus.OK);
        }

        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    // Get messages in a topic (ordered by `indexInTopic`)
    @GetMapping("/{topicId}/messages")
    public ResponseEntity<List<Message>> getMessagesInTopic(@PathVariable Long topicId) {
        Optional<Topic> topicOpt = topicRepo.findById(topicId);

        if (topicOpt.isPresent()) {
            Topic topic = topicOpt.get();

            List<Message> orderedMessages = topic.getMessages()
                    .stream()
                    .sorted((m1, m2) -> Integer.compare(m1.getIndexInTopic(), m2.getIndexInTopic()))
                    .collect(Collectors.toList());

            return new ResponseEntity<>(orderedMessages, HttpStatus.OK);
        }

        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    // Remove a message from a topic & update indices
    @DeleteMapping("/{topicId}/messages/{messageId}")
    public ResponseEntity<Topic> removeMessageFromTopic(@PathVariable Long topicId, @PathVariable Long messageId) {
        Optional<Topic> topicOpt = topicRepo.findById(topicId);
        Optional<Message> messageOpt = messageRepo.findById(messageId);

        if (topicOpt.isPresent() && messageOpt.isPresent()) {
            Topic topic = topicOpt.get();
            Message message = messageOpt.get();

            // Remove the message from the topic
            topic.getMessages().remove(message);

            // Update `indexInTopic` for remaining messages
            int index = 1;
            for (Message msg : topic.getOrderedMessages()) {
                msg.setIndexInTopic(index++);
                messageRepo.save(msg);
            }

            topicRepo.save(topic);

            // Delete message if it is not in any other topic
            if (message.getTopics().isEmpty()) {
                messageRepo.delete(message);
            }

            return new ResponseEntity<>(topic, HttpStatus.OK);
        }

        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }
}
