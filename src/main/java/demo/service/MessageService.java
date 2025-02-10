package demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import demo.model.Message;
import demo.model.MessageQueue;
import demo.controller.MessageRepository;
import demo.controller.QueueRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);


    @Autowired
    private MessageRepository messageRepo;

    @Autowired
    private QueueRepository queueRepo;

    // ✅ Retrieve all messages in a queue (FIFO order)
    public List<Message> getMessagesByQueue(String queueId) {
        Optional<MessageQueue> queue = queueRepo.findById(queueId);
        return queue.map(messageRepo::findAllByQueueOrderByIdAsc).orElse(null);
    }

    // ✅ Get the next message from a queue (FIFO order)
    public Optional<Message> getNextMessage(String queueId) {
        List<Message> messages = getMessagesByQueue(queueId);
        return messages != null && !messages.isEmpty() ? Optional.of(messages.get(0)) : Optional.empty();
    }

    // ✅ Add a new message to a queue
    public Optional<Message> addMessageToQueue(String queueId, String content) {
        Optional<MessageQueue> queueOpt = queueRepo.findById(queueId);
        if (queueOpt.isPresent()) {
            MessageQueue queue = queueOpt.get();
            Message message = new Message(content);
            message.setQueue(queue);
            queue.addMessage(message);
            return Optional.of(messageRepo.save(message));
        }
        return Optional.empty();
    }

    // ✅ Mark message as read and log the event
    public Message readMessage(long messageId) {
        Optional<Message> messageOpt = Optional.ofNullable(messageRepo.findById(messageId));
        if (messageOpt.isPresent()) {
            Message message = messageOpt.get();
            message.markAsRead();
            messageRepo.save(message);

            // ✅ Logging message read event
            logger.info("Message {} read {} times. First accessed at: {}",
                    messageId, message.getReadCount(), message.getFirstAccessedAt());

            return message;
        }
        return null;
    }

    public String deleteMessage(Long messageId, String queueId) {
        Optional<Message> messageOpt = messageRepo.findById(messageId);
        if (messageOpt.isEmpty()) {
            return "Message not found.";
        }

        Message message = messageOpt.get();

        // ✅ Prevent deletion if the message has not been read
        if (!message.isRead()) {
            return "Message cannot be deleted because it has not been read.";
        }

        // ✅ Calculate deletion time
        LocalDateTime now = LocalDateTime.now();
        Duration timeToDelete = Duration.between(message.getCreatedAt(), now);

        // ✅ Check if the message exists in other queues
        boolean existsInOtherQueues = queueRepo.findAllBy().stream()
                .anyMatch(q -> q.getMessages().contains(message));

        // ✅ If the message exists in another queue, do not delete it completely
        if (!existsInOtherQueues) {
            messageRepo.delete(message);
            logger.info("Deleted Message {} after {} seconds. Read count: {}",
                    messageId, timeToDelete.getSeconds(), message.getReadCount());
        } else if (queueId != null) {
            // ✅ Remove the message only from the specified queue
            Optional<MessageQueue> queueOpt = queueRepo.findById(queueId);
            if (queueOpt.isPresent()) {
                MessageQueue queue = queueOpt.get();
                queue.removeMessage(messageId);
                queueRepo.save(queue);
                return "Message removed from queue but exists in other queues.";
            }
        }

        return "SUCCESS";
    }

    // ✅ Get messages starting from a given ID
    public List<Message> getMessagesFrom(Long startId) {
        return messageRepo.findByIdGreaterThanEqual(startId);
    }

    // ✅ Search messages by partial content
    public List<Message> searchMessages(String keyword) {
        return messageRepo.findByTextContaining(keyword);
    }
}
