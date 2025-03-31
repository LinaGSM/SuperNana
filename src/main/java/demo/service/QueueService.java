package demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import demo.model.Message;
import demo.model.Queue;
import demo.repository.QueueRepository;
import demo.repository.MessageRepository;
import org.springframework.transaction.annotation.Transactional;


import java.util.Collection;
import java.util.Optional;

/**
 * Service layer for managing queues and their associated messages.
 */
@Service
public class QueueService {

    @Autowired
    private QueueRepository queueRepo;

    @Autowired
    private MessageRepository messageRepo;

    private static final Logger logger = LoggerFactory.getLogger(QueueService.class);


    // Methods

    /**
     * Creates a new queue with the given ID.
     *
     * @param id The id of the queue to be created.
     * @return The created queue.
     */
    @Transactional
    public Queue createQueue(String id){
        Queue queue = new Queue(id);
        Queue createdQueue = queueRepo.save(queue);
        queueRepo.flush();

        logger.info("Successfully created queue {} ", createdQueue.getId());
        return createdQueue;
    }


    /**
     * Deletes a queue by its ID.
     * @param id The id of the queue to be deleted.
     */
    @Transactional
    public void deleteQueue(String id) {
        queueRepo.deleteById(id);
        logger.info("Successfully deleted queue {} ", id);
    }


    /**
     * Retrieves all queues, optionally filtering by a given prefix.
     *
     * @param prefix The prefix to filter queues by. If null or empty, all queues are returned.
     * @return A collection of queues matching the criteria.
     */
    @Transactional
    public Collection<Queue> getAllQueues(String prefix) {
        return queueRepo.findAllBy().stream()
                .filter(q -> q.getId().startsWith(prefix))
                .toList();
    }


    /**
     * Retrieves a queue by its ID.
     * @param id The identifier of the queue.
     */
    @Transactional
    public Optional<Queue> getQueue(String id) {
        return queueRepo.findById(id);
    }


    /**
     * Initializes test data by creating sample queues and messages.
     * This method sets up two queues ("main" and "secondary") and associates
     * several messages with each queue.
     */
    public void initializeTestData() {
        Message hi = new Message("Hi");
        Message hello = new Message("Hello");

        Queue queue = new Queue();
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

        Queue queue2 = new Queue();
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
