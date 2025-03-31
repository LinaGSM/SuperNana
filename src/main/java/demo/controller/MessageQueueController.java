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

    /**
     * Creates a new message queue.
     * @param name The name of the queue to create
     * @return The created queue
     */
    @PostMapping
    public ResponseEntity<Queue> createQueue(@RequestBody String name) {
        Queue queue = queueService.createQueue(name);
        return new ResponseEntity<>(queue, HttpStatus.CREATED);
    }


    /**
     * Retrieves a queue by its ID.
     * @param id The ID of the queue to retrieve
     */
    @GetMapping("/{id}")
    public ResponseEntity<Queue> getQueue(@PathVariable("id") String id) {
        Optional<Queue> queue = queueService.getQueue(id);
        return queue.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    /**
     * Retrieves all queues, optionally filtered by a name prefix.
     * @param prefix Optional prefix to filter queue names (case-sensitive)
     */
    @GetMapping
    public ResponseEntity<Collection<Queue>> getQueues(
            @RequestParam(value = "startWith", defaultValue = "") String prefix) {
        return new ResponseEntity<>(queueService.getAllQueues(prefix), HttpStatus.OK);
    }


    /**
     * Retrieves all messages from a specific queue.
     * @param queueId The ID of the queue
     */
    @GetMapping("/{id}/messages")
    public ResponseEntity<List<Message>> getMessages(@PathVariable("id") String queueId) {
        List<Message> messages = queueMessageService.getMessagesByQueue(queueId);
        return messages != null ? new ResponseEntity<>(messages, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Marks a message as read.
     * @param messageId The ID of the message to mark as read
     * @return The message marked as true
     */
    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<Message> readMessage(@PathVariable("messageId") long messageId) {
        Message message = messageService.readMessage(messageId);
        return message != null ? new ResponseEntity<>(message, HttpStatus.OK) :
                                 new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }


    /**
     * Retrieves the next message in a queue (FIFO order).
     * @param queueId The ID of the queue
     */
    @GetMapping("/{id}/messages/next")
    public ResponseEntity<Message> getNextMessage(@PathVariable("id") String queueId) {
        Optional<Message> message = queueMessageService.getNextMessageInQueue(queueId);
        return message.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Adds a new message to a queue.
     * @param queueId The ID of the target queue
     * @param content The content of the message to add
     * @return The updated queue containing the new message
     */
    @PostMapping("/{id}/messages")
    public ResponseEntity<Queue> addMessageToQueue(@PathVariable("id") String queueId, @RequestBody String content) {
        Optional<Queue> result = queueMessageService.addMessageToQueue(queueId, content);
        return result.map(value -> new ResponseEntity<>(value, HttpStatus.CREATED))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    /**
     * Deletes a message from a queue.
     * @param queueId The ID of the queue
     * @param msgId The ID of the message to delete
     * @return The updated queue not containing the message anymore
     */
    @DeleteMapping("/{id}/messages/{msgId}")
    public ResponseEntity<Queue> deleteMessageFromQueue(@PathVariable("id") String queueId, @PathVariable("msgId") long msgId) {
        Optional<Queue> result = queueMessageService.deleteMessageFromQueue(queueId, msgId);
        return result.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.FORBIDDEN));
    }


    /**
     * Retrieves messages starting from a specific message ID.
     * @param startingFromMessageId The ID of the message to start from
     */
    @GetMapping("/messages")
    public ResponseEntity<List<Message>> getMessagesStartingFrom(@RequestParam("startFrom") Long startingFromMessageId) {
        List<Message> messages = messageService.getMessagesFrom(startingFromMessageId);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    /**
     * Searches messages by content keyword (case-sensitive).
     * @param keyword The search term to match against message content
     */
    @GetMapping("/messages/search")
    public ResponseEntity<List<Message>> searchMessages(@RequestParam("content") String keyword) {
        List<Message> messages = messageService.searchMessages(keyword);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }
}
