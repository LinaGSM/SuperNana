package demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import demo.model.Message;
import demo.model.MessageQueue;
import demo.controller.MessageRepository;
import demo.controller.QueueRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

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

    // ✅ Delete a message only if it has been read and is not linked to another queue
    public String deleteMessage(String queueId, long messageId) {
        Optional<MessageQueue> queueOpt = queueRepo.findById(queueId);
        if (queueOpt.isPresent()) {
            MessageQueue queue = queueOpt.get();
            Optional<Message> messageOpt = queue.removeMessage(messageId);

            if (messageOpt.isPresent()) {
                Message message = messageOpt.get();

                // Check if the message has been read before deleting
                if (!message.isRead()) {
                    return "Message cannot be deleted because it has not been read.";
                }

                // Check if the message exists in other queues
                boolean existsInOtherQueues = queueRepo.findAllBy().stream()
                        .anyMatch(q -> q.getMessages().contains(message));

                if (!existsInOtherQueues) {
                    messageRepo.delete(message);
                }

                queueRepo.save(queue);
                return "SUCCESS";
            }
        }
        return "Message not found.";
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
