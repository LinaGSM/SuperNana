package demo.controller;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import demo.model.Topic;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import demo.model.Queue;
import demo.model.Message;
import demo.service.MessageService;
import demo.service.QueueService;

@RestController
@RequestMapping("/queues")
public class MessageQueueController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private QueueService queueService;

    // Initialize test data (for demo purposes)
    @PostConstruct
    private void init() {
        queueService.initializeTestData();
    }

    // Create a new queue
    @PostMapping
    public ResponseEntity<Topic> createQueue(@RequestBody String name) {
        Topic topic = topicService.createTopic(name);
        return new ResponseEntity<>(topic, HttpStatus.CREATED);
    }

    // Get all queues with an optional filter by prefix
    @GetMapping
    public ResponseEntity<Collection<Queue>> getQueues(
            @RequestParam(value = "startWith", defaultValue = "") String prefix) {
        return new ResponseEntity<>(queueService.getAllQueues(prefix), HttpStatus.OK);
    }

    // Get all messages from a queue
    @GetMapping("/{id}/messages")
    public ResponseEntity<List<Message>> getMessages(@PathVariable("id") String id) {
        List<Message> messages = messageService.getMessagesByQueue(id);
        return messages != null ? new ResponseEntity<>(messages, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Read a message and update metadata
    @GetMapping("/messages/{messageId}/read")
    public ResponseEntity<Message> readMessage(@PathVariable("messageId") long messageId) {
        Message message = messageService.readMessage(messageId);
        return message != null ? new ResponseEntity<>(message, HttpStatus.OK) :
                                 new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    // Get the next message in a queue (FIFO order)
    @GetMapping("/{id}/messages/next")
    public ResponseEntity<Message> getNextMessage(@PathVariable("id") String id) {
        Optional<Message> message = messageService.getNextMessage(id);
        return message.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Add a new message to a queue
    @PostMapping("/{id}/messages")
    public ResponseEntity<Message> addMessageToQueue(@PathVariable("id") String id, @RequestBody String content) {
        Optional<Message> message = messageService.addMessageToQueue(id, content);
        return message.map(value -> new ResponseEntity<>(value, HttpStatus.CREATED))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Delete a message from a queue only if it has been read and isn't in other queues
    @DeleteMapping("/{id}/messages/{msgId}")
    public ResponseEntity<String> deleteMessageFromQueue(@PathVariable("id") String id, @PathVariable("msgId") long msgId) {
        String result = messageService.deleteMessage(msgId, id);
        return result.equals("SUCCESS") ? new ResponseEntity<>(HttpStatus.OK) :
                new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
    }

    // Retrieve messages starting from a given number
    @GetMapping("/messages")
    public ResponseEntity<List<Message>> getMessagesStartingFrom(@RequestParam("startFrom") Long startId) {
        List<Message> messages = messageService.getMessagesFrom(startId);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    // Search messages by partial content
    @GetMapping("/messages/search")
    public ResponseEntity<List<Message>> searchMessages(@RequestParam("content") String keyword) {
        List<Message> messages = messageService.searchMessages(keyword);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }
}
