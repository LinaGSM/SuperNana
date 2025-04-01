package demo.service;

import demo.model.Message;
import demo.model.Topic;
import demo.repository.MessageRepository;
import demo.repository.TopicMessageAssociationRepository;
import demo.repository.TopicRepository;
import demo.model.TopicMessageAssociation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
    private TopicMessageAssociationRepository topicMessageAssociationRepo;


    private static final Logger logger = LoggerFactory.getLogger(TopicService.class);




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
     * Get a topic by ID
     * @param topicId The ID of the topic
     */
    public Topic getTopic(Long topicId) {
        logger.debug("Retrieving topic {}", topicId);
        return topicRepo.findById(topicId).orElse(null);
    }


    /**
     * Retrieves all messages associated with a specific topic, ordered by position index.
     *
     * @param topicId The ID of the topic.
     * @return The list of messages associated with the topic.
     */
    public Optional<List<Message>> getMessagesByTopic(Long topicId) {
        List<TopicMessageAssociation> associations =  topicMessageAssociationRepo.findByTopicId(topicId);
        List<Message> messages = new ArrayList<>();

        for (TopicMessageAssociation association : associations) {
            messages.add(association.getMessage());
        };

        // Get message index from association table to update the index position of messages
        updateMessagesIndexing(topicId);

        logger.info("Retrieved {} messages from association", messages.size());

        return Optional.of(messages) ;
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
        List<TopicMessageAssociation> associations = topicMessageAssociationRepo.findByTopicIdOrderByPositionIndexAsc(topicId);

        for (TopicMessageAssociation association : associations) {
            association.getMessage().setIndexInTopic(association.getPositionIndex());
            messageRepo.save(association.getMessage());
        }
    }
}
