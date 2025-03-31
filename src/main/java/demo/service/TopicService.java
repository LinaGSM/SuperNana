package demo.service;

import demo.model.Message;
import demo.model.Topic;
import demo.repository.MessageRepository;
import demo.repository.TopicRepository;
import demo.model.TopicMessageAssociation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;


/**
 * Service layer for managing topics and their associated messages.
 */
@Service
public class TopicService {

    @Autowired
    private TopicRepository topicRepo;

    @Autowired
    private MessageRepository messageRepo;

    @Autowired
    private TopicMessageAssociationService associationService;

    @Autowired
    private MessageService messageService;

    private static final Logger logger = LoggerFactory.getLogger(TopicService.class);


    // Methods

    /**
     * Creates a new topic with the given name.
     *
     * @param name The name of the topic to be created.
     * @return The created topic.
     */
    @Transactional
    public Topic createTopic(String name) {
        Topic topic = new Topic(name);
        Topic createdTopic = topicRepo.save(topic);

        logger.info("Successfully created topic {} with ID: {}", name, createdTopic.getId());
        return createdTopic;
    }


    /**
     * Adds a message to a specific topic.
     *
     * @param topicId   The ID of the topic.
     * @param messageId The ID of the message to be added.
     * @return The updated topic
     */
    @Transactional
    public Optional<Topic> addMessageToTopic(Long topicId, Long messageId) {
        Optional<Topic> topicOpt = topicRepo.findById(topicId);
        Optional<Message> messageOpt = messageRepo.findById(messageId);

        if (topicOpt.isPresent() && messageOpt.isPresent()) {
            Topic topic = topicOpt.get();
            Message message = messageOpt.get();

            if (!topic.isMessageInTopic(message)) {
                // Create a new association between topic and message
                associationService.createAssociation(topic.getId(), message.getId());

                // Add topic to message's associated topic list
                messageService.addToAssociatedTopic(message, topic);

                // Add message to topic
                topic.addMessage(message);
                // topicRepo.save(topic);
                // messageRepo.save(message);

                logger.info("Successfully added message {} to topic {}", messageId, topicId);
            }else {
                logger.warn("Message {} is already in topic {}", messageId, topicId);
            }

            // Get message index from association table to update the index position of messages
            updateMessagesIndexing(topicId);

            return Optional.of(topic);
        }

        if (topicOpt.isEmpty()) {
            logger.error("Topic {} was not found : ", topicId);
        }
        if (messageOpt.isEmpty()) {
            logger.error("Message {} was not found : ", messageId);
        }

        return Optional.empty();
    }



    /**
     * Retrieves all messages associated with a specific topic.
     * @param topicId The ID of the topic.
     */
    public Optional<List<Message>> getMessagesInTopic(Long topicId) {
        Optional<List<Message>> messages = associationService.getMessagesByTopic(topicId);

        // Get message index from association table to update the index position of messages
        updateMessagesIndexing(topicId);

        return messages ;
    }



    /**
     * Removes a message from a specific topic.
     *
     * @param topicId   The ID of the topic.
     * @param messageId The ID of the message to be removed.
     * @return The updated topic
     */
    @Transactional
    public Optional<Topic> removeMessageFromTopic(Long topicId, Long messageId) {
        Optional<Topic> topicOpt = topicRepo.findById(topicId);
        Optional<Message> messageOpt = messageRepo.findById(messageId);

        if (topicOpt.isPresent() && messageOpt.isPresent()) {
            Topic topic = topicOpt.get();
            Message message = messageOpt.get();

            // Delete the association between message and topic
            associationService.removeAssociation(topic.getId(), message.getId());

            // Remove topic from message's associated topic list
            messageService.removeFromAssociatedTopic(message, topic);

            // Remove message from the specified topic
            topic.removeMessage(message);

            // Save topic
            topicRepo.save(topic);
            logger.info("Successfully removed message {} from topic {}", messageId, topicId);

            // Delete message if it is not in any other topics
            messageService.safeDeleteMessageIfOrphanedInTopic(messageId);

            // Get message index from association table to update the index position of messages
            updateMessagesIndexing(topicId);

            return Optional.of(topic);
        }

        if (topicOpt.isEmpty()) {
            logger.error("Topic not found with ID: {}", topicId);
        }
        if (messageOpt.isEmpty()) {
            logger.error("Message not found with ID: {}", messageId);
        }

        return Optional.empty();
    }


    /**
     * Updates the index positions of messages within a specific topic.
     * This ensures that messages have the correct order in the given topic.
     * <p>
     * Each message can belong to multiple topics, and its index may vary
     * depending on the topic it is associated with. This method retrieves
     * the correct position index from the association table and updates
     * the corresponding messages accordingly.
     * </p>
     *
     * @param topicId The ID of the topic for which message indexes need to be updated.
     */
    public void updateMessagesIndexing(Long topicId) {
        List<TopicMessageAssociation> associations = associationService.getAssociationsByTopic(topicId);

        for (TopicMessageAssociation association : associations) {
            association.getMessage().setIndexInTopic(association.getPositionIndex());
            messageRepo.save(association.getMessage());
        }
    }
}
