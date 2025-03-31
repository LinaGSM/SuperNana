package demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import demo.model.Message;
import demo.model.Topic;
import demo.service.TopicService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/topics")
public class TopicController {

    @Autowired
    private TopicService topicService;


    /**
     * Creates a new topic with the given name.
     *
     * @param name The name of the new topic.
     * @return The created topic
     */
    @PostMapping
    public ResponseEntity<Topic> createTopic(@RequestBody String name) {
        Topic topic = topicService.createTopic(name);
        return new ResponseEntity<>(topic, HttpStatus.CREATED);
    }


    /**
     * Adds a message to a specific topic.
     *
     * @param topicId   The ID of the topic.
     * @param messageId The ID of the message to be added.
     * @return The updated topic
     */
    @PostMapping("/{topicId}/messages/{messageId}")
    public ResponseEntity<Topic> addMessageToTopic(@PathVariable Long topicId, @PathVariable Long messageId) {
        Optional<Topic> topic = topicService.addMessageToTopic(topicId, messageId);
        return topic.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }


    /**
     * Retrieves all messages associated with a specific topic.
     * @param topicId The ID of the topic.
     */
    @GetMapping("/{topicId}/messages")
    public ResponseEntity<List<Message>> getMessagesInTopic(@PathVariable Long topicId) {
        Optional<List<Message>> messages = topicService.getMessagesInTopic(topicId);
        return messages.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }


    /**
     * Removes a message from a specific topic.
     *
     * @param topicId   The ID of the topic.
     * @param messageId The ID of the message to be removed.
     * @return The updated topic
     */
    @DeleteMapping("/{topicId}/messages/{messageId}")
    public ResponseEntity<Topic> removeMessageFromTopic(@PathVariable Long topicId, @PathVariable Long messageId) {
        Optional<Topic> topic = topicService.removeMessageFromTopic(topicId, messageId);
        return topic.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }
}