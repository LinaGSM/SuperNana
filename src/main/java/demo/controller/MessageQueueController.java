package demo.controller;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import demo.model.Topic;
import demo.service.QueueMessageService;
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

    @Autowired
    private QueueMessageService queueMessageService;

    // Initialize test data (for demo purposes)
    @PostConstruct
    private void init() {
        queueService.initializeTestData();
    }

    // Create a new queue
    @PostMapping
    public ResponseEntity<Queue> createQueue(@RequestBody String name) {
        Queue queue = queueService.createQueue(name);
        return new ResponseEntity<>(queue, HttpStatus.CREATED);
    }

    // Get a queue by id
    @GetMapping("/{id}")
    public ResponseEntity<Queue> getQueue(@PathVariable("id") String id) {
        Optional<Queue> queue = queueService.getQueue(id);
        return queue.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Get all queues with an optional filter by prefix
    @GetMapping
    public ResponseEntity<Collection<Queue>> getQueues(
            @RequestParam(value = "startWith", defaultValue = "") String prefix) {
        return new ResponseEntity<>(queueService.getAllQueues(prefix), HttpStatus.OK);
    }

    // Get all messages from a queue
    @GetMapping("/{id}/messages")
    public ResponseEntity<List<Message>> getMessages(@PathVariable("id") String queueId) {
        List<Message> messages = queueMessageService.getMessagesByQueue(queueId);
        return messages != null ? new ResponseEntity<>(messages, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Read a message
    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<Message> readMessage(@PathVariable("messageId") long messageId) {
        Message message = messageService.readMessage(messageId);
        return message != null ? new ResponseEntity<>(message, HttpStatus.OK) :
                                 new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    // Get the next message in a queue (FIFO order)
    @GetMapping("/{id}/messages/next")
    public ResponseEntity<Message> getNextMessage(@PathVariable("id") String queueId) {
        Optional<Message> message = queueMessageService.getNextMessageInQueue(queueId);
        return message.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Add a new message to a queue
    @PostMapping("/{id}/messages")
    public ResponseEntity<Queue> addMessageToQueue(@PathVariable("id") String queueId, @RequestBody String content) {
        Optional<Queue> result = queueMessageService.addMessageToQueue(queueId, content);
        return result.map(value -> new ResponseEntity<>(value, HttpStatus.CREATED))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Delete a message from a queue
    @DeleteMapping("/{id}/messages/{msgId}")
    public ResponseEntity<Queue> deleteMessageFromQueue(@PathVariable("id") String queueId, @PathVariable("msgId") long msgId) {
        Optional<Queue> result = queueMessageService.deleteMessageFromQueue(queueId, msgId);
        return result.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.FORBIDDEN));
    }

    // Retrieve messages starting from a given number
    @GetMapping("/messages")
    public ResponseEntity<List<Message>> getMessagesStartingFrom(@RequestParam("startFrom") Long startingFromMessageId) {
        List<Message> messages = messageService.getMessagesFrom(startingFromMessageId);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    // Search messages by partial content ( Case sensitive)
    @GetMapping("/messages/search")
    public ResponseEntity<List<Message>> searchMessages(@RequestParam("content") String keyword) {
        List<Message> messages = messageService.searchMessages(keyword);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }
}
