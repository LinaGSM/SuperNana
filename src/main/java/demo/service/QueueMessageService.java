package demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import demo.model.Message;
import demo.model.Queue;
import demo.repository.MessageRepository;
import demo.repository.QueueRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Service for managing messages within queues
 */
@Service
public class QueueMessageService {
    @Autowired
    private MessageRepository messageRepo;

    @Autowired
    private QueueRepository queueRepo;

    @Autowired
    private MessageService messageService;

    @Autowired
    private QueueService queueService;

    private static final Logger logger = LoggerFactory.getLogger(QueueMessageService.class);



    /**
     * Adds a new message with the given content to a specified queue.
     *
     * @param queueId The ID of the queue where the message should be added.
     * @param content The content of the message to be created.
     * @return The updated queue
     */
    //@Transactional
    public Optional<Queue> addMessageToQueue(String queueId, String content) {
        Optional<Queue> queueOpt = queueService.getQueue(queueId);
        if (queueOpt.isPresent()) {
            Queue queue = queueOpt.get();
            Message message = messageService.createMessage(content);


            if(message.getQueue() == null && !getMessagesByQueue(queueId).contains(message)) { //FIXME No need too add those checks since we create a new and clean message, so it is not linked to any queue
                // add a message to a queue
                message.setQueue(queue);

                // Save
                messageRepo.save(message);
                queue.addMessage(message);

                queueRepo.save(queue);

            }else{
                logger.warn("Message {} is already in queue {}", message.getId(), queueId);
            }

            return Optional.of(queue);
        }

        logger.error("Queue {} was not found", queueId);
        return Optional.empty();
    }


    /**
     * Removes a message from a queue if it exists and has been marked as read.
     *
     * @param queueId The ID of the queue from which the message should be removed.
     * @param messageId The ID of the message to be removed.
     * @return The updated queue
     */
    @Transactional
    public Optional<Queue> deleteMessageFromQueue( String queueId, Long messageId) {
        Optional<Message> messageOpt = messageRepo.findById(messageId);
        Optional<Queue> queueOpt = queueRepo.findById(queueId);

        if (queueOpt.isPresent() && messageOpt.isPresent()) {
            Message message = messageOpt.get();
            Queue queue = queueOpt.get();

            // Check if message is present in queue AND message is marked as read
            if(getMessagesByQueue(queueId).contains(message) && message.getIsRead()) {
                // remove message from queue
                queue.removeMessage(message);
                messageService.deleteMessage(message);

                // save
                queueRepo.save(queue);

                logger.info("Successfully deleted message {} from queue {}", messageId, queueId);

            } else if (!getMessagesByQueue(queueId).contains(message)) {
                logger.warn("Cannot delete Message {} from queue {} : message is not in queue", messageId, queueId);
            } else {
                logger.warn("Cannot delete Message {} from queue {} : message is not marked as read", messageId, queueId);
            }

            return Optional.of(queue);
        }

        if (queueOpt.isEmpty()) {
            logger.error("Queue {} was not found", queueId);
        }

        if (messageOpt.isEmpty()) {
            logger.error("Message {} was not found", messageId);
        }

        return Optional.empty();

    }


    /**
     * Retrieves all messages in a given queue in FIFO order.
     *
     * @param queueId The ID of the queue.
     * @return A list of messages in the queue, ordered by their insertion order.
     */
    @Transactional
    public List<Message> getMessagesByQueue(String queueId) {
        Optional<Queue> queue = queueService.getQueue(queueId);
        return queue.map(messageRepo::findAllByQueueOrderByIdAsc).orElse(new ArrayList<>());
    }


    /**
     * Retrieves the next message in a queue (FIFO order).
     *
     * @param queueId The ID of the queue.
     * @return The next message
     */
    @Transactional
    public Optional<Message> getNextMessageInQueue(String queueId) {
        List<Message> messages = getMessagesByQueue(queueId);
        return messages != null && !messages.isEmpty() ? Optional.of(messages.get(0)) : Optional.empty();
    }

}
