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


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class TopicMessageAssociationService {

    @Autowired
    private TopicMessageAssociationRepository associationRepo;

    @Autowired
    private TopicRepository topicRepo;

    @Autowired
    private MessageRepository messageRepo;

    private static final Logger logger = LoggerFactory.getLogger(TopicMessageAssociationService.class);

    // Methods

    // Method : Create an association between a message and a topic
    @Transactional
    public TopicMessageAssociation createAssociation(Long topicId, Long messageId) {
        Topic topic = topicRepo.findById(topicId)
                .orElseThrow(() -> {
                    logger.error("Topic not found with id: {}", topicId);
                    return new IllegalArgumentException("Topic not found");
                });

        Message message = messageRepo.findById(messageId)
                .orElseThrow(() -> {
                    logger.error("Message not found with id: {}", messageId);
                    return new IllegalArgumentException("Message not found");
                });

        int nextPosition = associationRepo.countByTopicId(topicId) + 1;

        // Save the new association in the table
        TopicMessageAssociation association = new TopicMessageAssociation(topic, message, nextPosition);
        TopicMessageAssociation savedAssociation = associationRepo.save(association);
        logger.info("Created association between topic {} and message {} at position {}", topicId, messageId, nextPosition);

        return savedAssociation;
    }

    // Method : Get all the association related to a topic
    public List<TopicMessageAssociation> getAssociationsByTopic(Long topicId) {
        return associationRepo.findByTopicIdOrderByPositionIndexAsc(topicId);
    }


    // Method : Get all the messages of a topic
    public Optional<List<Message>> getMessagesByTopic(Long topicId) {
        List<TopicMessageAssociation> associations = getAssociationsByTopic(topicId);
        List<Message> messages = new ArrayList<>();

        for (TopicMessageAssociation association : associations) {
            messages.add(association.getMessage());
        }

        return Optional.of(messages);
    }


    // Method : Remove association from the table
    @Transactional
    public void removeAssociation(Long topicId, Long messageId) {
        int deletedPosition = associationRepo.findPositionIndex(topicId, messageId)
                .orElseThrow(() -> {
                    logger.error("Association not found in table for topic {} and message {}", topicId, messageId);
                    return new IllegalArgumentException("Association not found");
                });

        // Delete from table
        associationRepo.deleteByTopicIdAndMessageId(topicId, messageId);
        logger.info("Deleted association between topic {} and message {}", topicId, messageId);

        // Reorganise position starting from the deleted position (instead of the beginning)
        associationRepo.decrementPositionsStartingFrom(topicId, deletedPosition);
        logger.debug("Reorganized positions for topic {}", topicId);

    }









}
