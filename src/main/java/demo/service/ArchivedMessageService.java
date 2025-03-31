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

@Service
public class ArchivedMessageService {
    private static final Logger logger = LoggerFactory.getLogger(ArchivedMessageService.class);

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

    // Manual trigger
    @Transactional
    public List<ArchivedMessage> archiveNow() {
        logger.info("Manual Archiving now");
        return archiveOldMessages();
    }

    // Automatic schedule (runs daily at 3AM)
    @Scheduled(cron = "${app.archive.cron:0 0 3 * * ?}")
    @Transactional
    public void autoArchive() {
        logger.info("Auto Archiving now...");
        archiveOldMessages();
    }

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

    // Get all the archived Messages
    public List<ArchivedMessage> getAllArchivedMessages() {
        List<ArchivedMessage> archived = archiveRepo.findAll();
        if (archived.isEmpty()) {
            logger.info("No Archived Messages Found");
        }
        return archived;
    }

    // Get archived messages from the same queue
    public List<ArchivedMessage> getArchivedMessagesByQueueId(String queueId) {
        List<ArchivedMessage> archived = archivedMessageRepo.findByOriginalQueueId(queueId);
        if (archived.isEmpty()) {
            logger.info("No Archived Messages Found for Queue: {}", queueId);
        }
        return archived;
    }

    // Get archived messages containing keyword
    public List<ArchivedMessage> searchArchivedMessagesContainingKeyword(String keyword) {
        List<ArchivedMessage> archived = archiveRepo.findByOriginalContentContaining(keyword);
        if (archived.isEmpty()) {
            logger.info("No Archived Messages Found containing Keyword: {}", keyword);
        }
        return archived;
    }
}