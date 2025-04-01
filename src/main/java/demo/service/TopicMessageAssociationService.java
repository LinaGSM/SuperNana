package demo.service;

import demo.repository.TopicMessageAssociationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import demo.model.Topic;
import demo.model.Message;
import demo.model.TopicMessageAssociation;
import demo.repository.TopicRepository;
import demo.repository.MessageRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Service layer responsible for managing index position of a message that can be part of different topic
 */
@Service
public class TopicMessageAssociationService {

    @Autowired
    private TopicMessageAssociationRepository associationRepo;

    @Autowired
    private TopicRepository topicRepo;

    @Autowired
    private MessageRepository messageRepo;


    private static final Logger logger = LoggerFactory.getLogger(TopicMessageAssociationService.class);



    /**
     * Add a message to a topic by creating an association between a given message and a topic.
     * The message is assigned the next available position index within the topic.
     *
     * @param topicId   The ID of the topic.
     * @param messageId The ID of the message.
     * @throws IllegalArgumentException if the topic or message does not exist.
     */
    @Transactional
    public Optional<Topic> addMessageToTopic(Long topicId, Long messageId) {
        if (associationExists(topicId, messageId)) {
            logger.warn("Association already exists between topic {} and message {}", topicId, messageId);
        }

        Topic topic = topicRepo.findById(topicId)
                .orElseThrow(() -> {
                    logger.error("Topic with id was not found : {}", topicId);
                    return null;
                });

        Message message = messageRepo.findById(messageId)
                .orElseThrow(() -> {
                    logger.error("Message with id was not found : {}", messageId);
                    return null ;
                });

        int nextPosition = associationRepo.countByTopicId(topicId) + 1;

        // Save the new association in the table
        TopicMessageAssociation association = new TopicMessageAssociation(topic, message, nextPosition);

        // Critical bidirectional maintenance
        topic.getMessageAssociations().add(association);
        message.getTopicAssociations().add(association);

        // No need of explicit save because of cascading ALL op
        // associationRepo.save(association);
        logger.info("Created association between topic {} and message {} at position {}", topicId, messageId, nextPosition);

        return Optional.of(topic);
    }



    /**
     * Retrieves all associations related to a specific topic, ordered by position index.
     *
     * @param topicId The ID of the topic.
     * @return A list of TopicMessageAssociation objects associated with the topic.
     */
    public List<TopicMessageAssociation> getAssociationsByTopic(Long topicId) {
        return associationRepo.findByTopicIdOrderByPositionIndexAsc(topicId);
    }



    /**
     * Remove a message from a topic by removing the association between a topic and a message
     * Reorders the message positions accordingly.
     *
     * @param topicId   The ID of the topic.
     * @param messageId The ID of the message.
     * @throws IllegalArgumentException if the association does not exist.
     */
    @Transactional
    public Optional<Topic> removeMessageFromTopic(Long topicId, Long messageId) {
        if (!associationExists(topicId, messageId)) {
            logger.error("Association doesn't exist between topic {} and message {}", topicId, messageId);
        }

        Topic topic = topicRepo.findById(topicId)
                .orElseThrow(() -> {
                    logger.error("Topic not found with id: {}", topicId);
                    return null;
                });

        Message message = messageRepo.findById(messageId)
                .orElseThrow(() -> {
                    logger.error("Message with id was not found : {}", messageId);
                    return null ;
                });

        TopicMessageAssociation association = topic.getMessageAssociations().stream()
                .filter(a -> a.getMessage().equals(message))
                .findFirst().orElse(null);

        if (association == null) {
            logger.error("Association doesn't exist between topic {} and message {}", topicId, messageId);
            return null;
        }


        int deletedPosition = associationRepo.findPositionIndex(topicId, messageId)
                .orElseThrow(() -> {
                    logger.error("Association not found in table for topic {} and message {}", topicId, messageId);
                    return null;
                });

        // JPA will handle deletion

        // Maintain bidirectional relationship
        topic.getMessageAssociations().remove(association);
        message.getTopicAssociations().remove(association);

        logger.info("Deleted association between topic {} and message {}", topicId, messageId);

        // Reorganise position starting from the deleted position (instead of the beginning)
        associationRepo.decrementPositionsStartingFrom(topicId, deletedPosition);
        logger.debug("Reorganized positions for topic {}", topicId);

        return topicRepo.findById(topicId);
    }



    /**
     * Verify the existence of an association
     * @param topicId The ID of the topic
     * @param messageId The ID of the message
     */
    public boolean associationExists(Long topicId, Long messageId) {
        return associationRepo.existsByTopicIdAndMessageId(topicId, messageId);
    }





}
