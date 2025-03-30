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




    // Method : Add a message to a queue
    //@Transactional
    public Optional<Queue> addMessageToQueue(String queueId, String content) {
        Optional<Queue> queueOpt = queueService.getQueue(queueId);
        if (queueOpt.isPresent()) {
            System.out.println("inside queue exist");
            Queue queue = queueOpt.get();
            Message message = messageService.createMessage(content);
            System.out.println("after creating message");


            if(message.getQueue() == null && !getMessagesByQueue(queueId).contains(message)) { //FIXME No need too add those checks since we create a new and clean message, so it is not linked to any queue
                // add a message to a queue
                System.out.println(" inside can add message");
                message.setQueue(queue);
                System.out.println("after adding message");

                // Save
                messageRepo.save(message);
                queue.addMessage(message);

                queueRepo.save(queue);
                System.out.println("after saving message");

                logger.info("Successfully added message {} to queue {}", message.getId(), queueId);
            }else{
                logger.warn("Message {} is already in queue {}", message.getId(), queueId);
            }
            System.out.println("good end");

            return Optional.of(queue);
        }
        System.out.println("bad end");

        logger.error("Queue {} was not found", queueId);
        return Optional.empty();
    }


    // Method : Delete message from queue
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



    // Method : Retrieve all messages in a queue (FIFO order)
    @Transactional
    public List<Message> getMessagesByQueue(String queueId) {
        Optional<Queue> queue = queueService.getQueue(queueId);
        return queue.map(messageRepo::findAllByQueueOrderByIdAsc).orElse(new ArrayList<>());
    }

    // Method : Get the next message from a queue (FIFO order)
    @Transactional
    public Optional<Message> getNextMessageInQueue(String queueId) {
        List<Message> messages = getMessagesByQueue(queueId);
        return messages != null && !messages.isEmpty() ? Optional.of(messages.get(0)) : Optional.empty();
    }

}
