package demo.service;

import demo.model.Topic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import demo.model.Message;
import demo.repository.MessageRepository;
import demo.repository.QueueRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepo;

    @Autowired
    private QueueRepository queueRepo;

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");



    // Methods

    /* -------------------------------
       Méthodes de base (CRUD + état)
       ------------------------------- */

    // Create a message
    public Message createMessage(String content) {
        Message message = new Message(content);
        return messageRepo.save(message);
    }


    // Delete message
    @Transactional
    public void deleteMessage(Message message) {
        if(message == null) {
            throw new IllegalArgumentException("The message cannot be null");
        }

        // Display log
        logDeletionStatistics(message);

        // Delete message
        messageRepo.delete(message);
    }


    // Mark message as read
    @Transactional
    public Message readMessage(long messageId) {
        Optional<Message> messageOpt = Optional.ofNullable(messageRepo.findById(messageId));
        if (messageOpt.isPresent()) {
            Message message = messageOpt.get();

            // mark message as read
            message.markAsRead();
            messageRepo.save(message);

            // Logging message read event
            logger.info("Message {} read {} times. First accessed at: {}",
                    messageId, message.getReadCount(), message.getFirstAccessedAt().format(formatter));

            return message;
        }
        return null;
    }




    /* -------------------------------
       Méthodes pour les recherche
       ------------------------------- */

    // Get messages starting from a given ID
    public List<Message> getMessagesFrom(Long startId) {
        return messageRepo.findByIdGreaterThanEqual(startId);
    }

    // Search messages by partial content
    public List<Message> searchMessages(String keyword) {
        return messageRepo.findByTextContaining(keyword);
    }




      /* -------------------------------
       Méthodes pour les topics
       ------------------------------- */

    // Add a topic to the list of topic associated with message
    public void addToAssociatedTopic(Message message, Topic topic) {
        message.getAssociatedTopics().add(topic);
    }

    // Remove a topic from the list of topic associated with message
    public void removeFromAssociatedTopic(Message message, Topic topic) {
        message.getAssociatedTopics().remove(topic);
    }

    // delete message if it's not in any topic
    @Transactional
    public void safeDeleteMessageIfOrphanedInTopic(Long messageId) {
        Message message = messageRepo.findById(messageId)
                .orElseThrow(() -> {
                    logger.error("Message not found with ID: {}", messageId);
                    return new IllegalArgumentException("Message not found");
                });

        if (message.getAssociatedTopics().isEmpty()) {
            logger.debug("Message {} is not associated with any topics - deleting", messageId);

            // delete message
            deleteMessage(message);
        } else {
            logger.warn("Message {} is still associated with topics - preserving", messageId);
        }
    }



    /* -------------------------------
       Méthodes privées (logging)
       ------------------------------- */

    private void logDeletionStatistics(Message message) {
        LocalDateTime deletedAt = LocalDateTime.now();
        Duration lifeTime = Duration.between(message.getCreatedAt(), LocalDateTime.now());

        logger.info("[STATS] Message deleted:\n" +
            "\t - ID: {} \n" +
            "\t - CreatedAt: {} \n" +
            "\t - FirstAccessedAt: {} \n" +
            "\t - DeletedAt: {} \n" +
            "\t - Lifetime: {} days, {}h {}min {}s \n" +
            "\t - Read count: {}"
            ,
                message.getId(),
                message.getCreatedAt().format(formatter),
                message.getFirstAccessedAt().format(formatter),
                deletedAt.format(formatter),
                lifeTime.toDays(),
                lifeTime.toHours() % 24,
                lifeTime.toMinutes() % 60,
                lifeTime.getSeconds() % 60,
                message.getReadCount()
        );
    }
}
