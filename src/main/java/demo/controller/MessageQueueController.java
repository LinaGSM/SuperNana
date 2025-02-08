package demo.controller;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import demo.model.MessageQueue;
import jakarta.annotation.PostConstruct;
import demo.model.Message;

@RestController
@RequestMapping("/queues")
public class MessageQueueController {
    @Autowired
    MessageRepository mRepo;
    @Autowired
    QueueRepository qRepo;

    @PostConstruct
    private void init() {
        Message hi = new Message("Hi");
        Message hello = new Message("Hello");

        MessageQueue queue = new MessageQueue();
        queue.setId("main");
        qRepo.save(queue);

        // ✅ Ensure messages are linked before saving
        hi.setQueue(queue);
        hello.setQueue(queue);
        hi.markAsRead();

        queue.addMessage(hi);
        queue.addMessage(hello);

        // ✅ Corrected Message constructors
        Message bonjour = new Message("Bonjour");
        Message hii = new Message("Hi");
        Message helllo = new Message("Hello");

        MessageQueue queue2 = new MessageQueue();
        queue2.setId("secondary");
        qRepo.save(queue2);

        bonjour.setQueue(queue2);
        hii.setQueue(queue2);
        helllo.setQueue(queue2);

        queue2.addMessage(bonjour);
        queue2.addMessage(hii);
        queue2.addMessage(helllo);

        // ✅ Save messages AFTER setting their queue
        mRepo.save(hi);
        mRepo.save(hello);
        mRepo.save(bonjour);
        mRepo.save(hii);
        mRepo.save(helllo);
        qRepo.save(queue);
        qRepo.save(queue2);
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Collection<MessageQueue>> getQueues(
            @RequestParam(value = "startWith", defaultValue = "") String prefix) {
        return new ResponseEntity<>(
                qRepo.findAllBy().stream().filter(x -> x.getId().startsWith(prefix)).toList(), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/messages", method = RequestMethod.GET)
    public ResponseEntity<List<Message>> getMessages(@PathVariable("id") String id) {
        Optional<MessageQueue> queue = qRepo.findById(id);
        if (queue.isPresent()) {
            List<Message> messages = mRepo.findAllByQueueOrderByIdAsc(queue.get()); // ✅ Ensure FIFO order
            return new ResponseEntity<>(messages, HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/{id}/messages/next", method = RequestMethod.GET)
    public ResponseEntity<Message> getNextMessage(@PathVariable("id") String id) {
        Optional<MessageQueue> queue = qRepo.findById(id);
        if (queue.isPresent()) {
            List<Message> messages = mRepo.findAllByQueueOrderByIdAsc(queue.get());
            if (!messages.isEmpty()) {
                return new ResponseEntity<>(messages.get(0), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/{id}/messages", method = RequestMethod.POST)
    public ResponseEntity<Message> addMessage(@PathVariable("id") String id, @RequestBody String contentString) {
        Optional<MessageQueue> o = qRepo.findById(id);
        if (o.isPresent()) {
            MessageQueue q = o.get();
            Message message = new Message(contentString);
            message.setQueue(q); // ✅ Ensure message is linked to queue
            q.addMessage(message);
            mRepo.save(message);
            return new ResponseEntity<>(message, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/{id}/messages/{msg}", method = RequestMethod.DELETE)
    public ResponseEntity<Message> deleteMessage(@PathVariable("id") String id, @PathVariable("msg") long mid) {
        Optional<MessageQueue> o = qRepo.findById(id);
        if (o.isPresent()) {
            MessageQueue queue = o.get();
            Optional<Message> messageToRemove = queue.removeMessage(mid);

            if (messageToRemove.isPresent()) {
                Message message = messageToRemove.get();

                // ✅ Check if the message has been read before deleting
                if (!message.isRead()) {
                    return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
                }

                boolean existsInOtherQueues = qRepo.findAllBy().stream()
                        .anyMatch(q -> q.getMessages().contains(message));

                if (!existsInOtherQueues) {
                    mRepo.delete(message); // ✅ Only delete if it doesn't exist in other queues
                }

                qRepo.save(queue); // Always save queue changes
                return new ResponseEntity<>(null, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    // ✅ Retrieve messages starting from a given number
    @RequestMapping(value = "/messages", method = RequestMethod.GET)
    public ResponseEntity<List<Message>> getMessagesFrom(@RequestParam("startFrom") Long startId) {
        List<Message> messages = mRepo.findByIdGreaterThanEqual(startId);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    // ✅ Search messages by partial content
    @RequestMapping(value = "/messages/search", method = RequestMethod.GET)
    public ResponseEntity<List<Message>> searchMessages(@RequestParam("content") String keyword) {
        List<Message> messages = mRepo.findByTextContaining(keyword);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }
}
