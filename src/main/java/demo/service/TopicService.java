package demo.service;

import demo.model.Message;
import demo.model.Topic;
import demo.controller.MessageRepository;
import demo.controller.TopicRepository;
import demo.model.TopicMessageAssociation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

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

    // Method : Create a topic
    @Transactional
    public Topic createTopic(String name) {
        Topic topic = new Topic(name);
        Topic createdTopic = topicRepo.save(topic);

        logger.info("Successfully created topic {} with ID: {}", name, createdTopic.getId());
        return createdTopic;
    }



    @Transactional
    // Method : Add a message in a topic
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
                topicRepo.save(topic);
                messageRepo.save(message);

                logger.info("Successfully added message {} to topic {}", messageId, topicId);
            }else {
                logger.warn("Message {} is already in topic {}", messageId, topicId);
            }

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



    // Method : Get all the messages from a topic
    public Optional<List<Message>> getMessagesInTopic(Long topicId) {
        Optional<List<Message>> messages = associationService.getMessagesByTopic(topicId);

        // Get message index from association table to update the index position of messages
        updateMessagesIndexing(topicId);

        return messages ;
    }



    // Method : Remove a message from a topic
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
            messageService.safeDeleteIfOrphanedInTopic(messageId);

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



    // Method : Update the index position of messages for display
    public void updateMessagesIndexing(Long topicId) {
        List<TopicMessageAssociation> associations = associationService.getAssociationsByTopic(topicId);

        for (TopicMessageAssociation association : associations) {
            association.getMessage().setIndexInTopic(association.getPositionIndex());
            messageRepo.save(association.getMessage());
        }
    }
}
