package demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import demo.model.ArchivedMessage;
import demo.model.Message;
import demo.repository.ArchivedMessageRepository;
import demo.repository.MessageRepository;
import demo.repository.QueueRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * Service layer for managing archived messages.
 */
@Service
public class ArchivedMessageService {
    private static final Logger logger = LoggerFactory.getLogger(ArchivedMessageService.class);

    /**
     * Number of days after which messages are archived.
     * Default value is 7 days if not set in the application properties.
     */
    @Value("${app.archive.retention-days:7}")
    private int retentionDays;

    @Autowired
    private QueueRepository queueRepo;

    @Autowired
    private MessageRepository messageRepo;

    @Autowired
    private ArchivedMessageRepository archivedMessageRepo;

    @Autowired
    private ArchivedMessageRepository archiveRepo;


    /**
     * Manually triggers the archiving process.
     * @return The list of messages that were archived.
     */
    @Transactional
    public List<ArchivedMessage> archiveNow() {
        logger.info("Manual Archiving now");
        return archiveOldMessages();
    }


    /**
     * Automatically triggers the archiving process at a scheduled time.
     * <p>
     * Runs daily at 3 AM by default, but the schedule can be modified
     * via application properties.
     * </p>
     */
    @Scheduled(cron = "${app.archive.cron:0 0 3 * * ?}")
    @Transactional
    public void autoArchive() {
        logger.info("Auto Archiving now...");
        archiveOldMessages();
    }


    /**
     * Archives messages that are older than the configured retention period.
     * <p>
     * Only messages that have been read are considered for archiving.
     * </p>
     *
     * @return A list of messages that were archived.
     */
    private List<ArchivedMessage> archiveOldMessages() {
        if (retentionDays < 0) return null;

        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);

        //  Only get old messages that were read
        List<Message> oldMessages = messageRepo.findByIsReadTrueAndCreatedAtBefore(cutoff);
        List<ArchivedMessage> archivedMessages = new ArrayList<>();

        oldMessages.forEach(msg -> {
            ArchivedMessage archived = new ArchivedMessage(
                    msg.getText(),
                    msg.getCreatedAt(),
                    msg.getQueue() != null ? msg.getQueue().getId() : null,
                    msg.getReadCount()
            );
            archiveRepo.save(archived);
            messageRepo.delete(msg);
            // Add messages in Archived message list
            archivedMessages.add(archived);


        });

        logger.info("Archived {} messages", archivedMessages.size());

        return archivedMessages;
    }

    /**
     * Retrieves all archived messages.
     * @return A list of all archived messages
     */
    public List<ArchivedMessage> getAllArchivedMessages() {
        List<ArchivedMessage> archived = archiveRepo.findAll();
        if (archived.isEmpty()) {
            logger.info("No Archived Messages Found");
        }
        return archived;
    }


    /**
     * Retrieves archived messages that were originally from a specific queue.
     *
     * @param queueId The ID of the queue.
     * @return A list of archived messages associated with the given queue.
     */
    public List<ArchivedMessage> getArchivedMessagesByQueueId(String queueId) {
        List<ArchivedMessage> archived = archivedMessageRepo.findByOriginalQueueId(queueId);
        if (archived.isEmpty()) {
            logger.info("No Archived Messages Found for Queue: {}", queueId);
        }
        return archived;
    }


    /**
     * Searches for archived messages containing a specific keyword.
     *
     * @param keyword The keyword to search for.
     * @return A list of archived messages containing the specified keyword.
     */
    public List<ArchivedMessage> searchArchivedMessagesContainingKeyword(String keyword) {
        List<ArchivedMessage> archived = archiveRepo.findByOriginalContentContaining(keyword);
        if (archived.isEmpty()) {
            logger.info("No Archived Messages Found containing Keyword: {}", keyword);
        }
        return archived;
    }
}