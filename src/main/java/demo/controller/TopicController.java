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

    // Create a new topic
    @PostMapping
    public ResponseEntity<Topic> createTopic(@RequestBody String name) {
        Topic topic = topicService.createTopic(name);
        return new ResponseEntity<>(topic, HttpStatus.CREATED);
    }

    // Add a message to a topic
    @PostMapping("/{topicId}/messages/{messageId}")
    public ResponseEntity<Topic> addMessageToTopic(@PathVariable Long topicId, @PathVariable Long messageId) {
        Optional<Topic> topic = topicService.addMessageToTopic(topicId, messageId);
        return topic.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    // Get messages in a topic
    @GetMapping("/{topicId}/messages")
    public ResponseEntity<List<Message>> getMessagesInTopic(@PathVariable Long topicId) {
        Optional<List<Message>> messages = topicService.getMessagesInTopic(topicId);
        return messages.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    // Remove a message from a topic
    @DeleteMapping("/{topicId}/messages/{messageId}")
    public ResponseEntity<Topic> removeMessageFromTopic(@PathVariable Long topicId, @PathVariable Long messageId) {
        Optional<Topic> topic = topicService.removeMessageFromTopic(topicId, messageId);
        return topic.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }
}