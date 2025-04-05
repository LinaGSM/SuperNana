package demo.service;

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


/**
 * Service layer for message-related operations including CRUD, searching, and topic associations.
 */
@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepo;

    @Autowired
    private QueueRepository queueRepo;

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");





    /* -------------------------------
       Basic Methods
       ------------------------------- */



    /**
     * Creates a new message with the specified content.
     *
     * @param content The text content of the message
     * @return The newly created Message entity
     */
    public Message createMessage(String content) {
        Message message = new Message(content);
        return messageRepo.save(message);
    }


    /**
     * Deletes a message from the system
     * Logs deletion statistics.
     *
     * @param message The message to delete
     * @throws IllegalArgumentException if the message is null
     */
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


    /**
     * Marks a message as read .
     *
     * @param messageId The ID of the message to mark as read
     * @return The updated Message entity, or null if not found
     */
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
       Search Methods
       ------------------------------- */



    /**
     * Retrieves messages with IDs greater than or equal to the specified ID.
     *
     * @param startId The minimum message ID to include in results
     * @return List of messages matching the criteria
     */
    public List<Message> getMessagesFrom(Long startId) {
        return messageRepo.findByIdGreaterThanEqual(startId);
    }


    /**
     * Searches messages containing the specified keyword in their content.
     * Note: Search is case-sensitive.
     *
     * @param keyword The text to search for in message content
     * @return List of messages containing the keyword
     */
    public List<Message> searchMessages(String keyword) {
        return messageRepo.findByTextContaining(keyword);
    }



      /* -------------------------------
       Topic Association Methods
       ------------------------------- */


    /**
     * Safely deletes a message only if it's not associated with any topics.
     *
     * @param messageId The ID of the message to potentially delete
     * @throws IllegalArgumentException if the message is not found
     */
    @Transactional
    public void safeDeleteMessageIfOrphanedInTopic(Long messageId) {
        Message message = messageRepo.findById(messageId)
                .orElseThrow(() -> {
                    logger.error("Message not found with ID: {}", messageId);
                    return new IllegalArgumentException("Message not found");
                });

        if (message.getTopicAssociations().isEmpty()) {
            logger.debug("Message {} is not associated with any topics - deleting", messageId);

            // delete message
            deleteMessage(message);
        } else {
            logger.warn("Message {} is still associated with topics - preserving", messageId);
        }
    }



    /* -------------------------------
       Private Methods (logging)
       ------------------------------- */


    /**
     * Logs detailed statistics about a message being deleted.
     *
     * @param message The message being deleted
     */
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
