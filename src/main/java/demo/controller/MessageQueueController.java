package demo.controller;

import java.util.Collection;
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
        Message hi = new Message();
        hi.setText("Hi");
        Message hello = new Message();
        hello.setText("Hello");

        MessageQueue queue = new MessageQueue();
        queue.setId("main");
        qRepo.save(queue);

        queue.addMessage(hi);
        queue.addMessage(hello);

        Message bonjour = new Message(3, "Bonjour");
        MessageQueue queue2 = new MessageQueue();
        queue2.setId("secondary");
        qRepo.save(queue2);

        queue2.addMessage(bonjour);

        mRepo.save(hi);
        mRepo.save(hello);
        mRepo.save(bonjour);
        qRepo.save(queue);
        qRepo.save(queue2);
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Collection<MessageQueue>> getProduct(
            @RequestParam(value = "startWith", defaultValue = "") String prefix) {
        return new ResponseEntity<>(
                qRepo.findAllBy().stream().filter(x -> x.getId().startsWith(prefix)).toList(), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/messages", method = RequestMethod.GET)
    public ResponseEntity<? extends Collection<Message>> getMessages(@PathVariable("id") String id) {
        return qRepo.findById(id).map(q -> new ResponseEntity<>(mRepo.findAllByQueue(q), HttpStatus.OK))
                .orElse(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    @RequestMapping(value = "/{id}/messages/next", method = RequestMethod.GET)
    public ResponseEntity<Message> getNextMessage(@PathVariable("id") String id) {
        return qRepo.findById(id).map(q -> new ResponseEntity<>(mRepo.findAllByQueue(q).get(0), HttpStatus.OK))
                .orElse(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    @RequestMapping(value = "/{id}/messages", method = RequestMethod.POST)
    public ResponseEntity<Message> addMessage(@PathVariable("id") String id, @RequestBody String contentString) {
        Optional<MessageQueue> o = qRepo.findById(id);
        if (o.isPresent()) {
            MessageQueue q = o.get();
            Message message = new Message();
            message.setText(contentString);
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
            MessageQueue q = o.get();
            Optional<Message> m = q.removeMessage(mid);
            if (m.isPresent()) {
                qRepo.save(q);
                mRepo.delete(m.get());
                return new ResponseEntity<>(null, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }
}
