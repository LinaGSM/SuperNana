package demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import demo.model.Message;
import demo.model.MessageQueue;
import demo.controller.QueueRepository;
import demo.controller.MessageRepository;

import jakarta.annotation.PostConstruct;
import java.util.Collection;

@Service
public class QueueService {

    @Autowired
    private QueueRepository queueRepo;

    @Autowired
    private MessageRepository messageRepo;

    //  Get all queues (filtering by prefix if provided)
    public Collection<MessageQueue> getAllQueues(String prefix) {
        return queueRepo.findAllBy().stream()
                .filter(q -> q.getId().startsWith(prefix))
                .toList();
    }

    //  Initialize test data (called in `@PostConstruct`)
    public void initializeTestData() {
        Message hi = new Message("Hi");
        Message hello = new Message("Hello");

        MessageQueue queue = new MessageQueue();
        queue.setId("main");
        queueRepo.save(queue);

        hi.setQueue(queue);
        hello.setQueue(queue);
        hi.markAsRead();

        queue.addMessage(hi);
        queue.addMessage(hello);

        Message bonjour = new Message("Bonjour");
        Message hii = new Message("Hi");
        Message helllo = new Message("Hello");

        MessageQueue queue2 = new MessageQueue();
        queue2.setId("secondary");
        queueRepo.save(queue2);

        bonjour.setQueue(queue2);
        hii.setQueue(queue2);
        helllo.setQueue(queue2);

        queue2.addMessage(bonjour);
        queue2.addMessage(hii);
        queue2.addMessage(helllo);

        // Save everything in the database
        messageRepo.save(hi);
        messageRepo.save(hello);
        messageRepo.save(bonjour);
        messageRepo.save(hii);
        messageRepo.save(helllo);
        queueRepo.save(queue);
        queueRepo.save(queue2);
    }
}
