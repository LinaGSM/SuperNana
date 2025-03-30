package demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;
import demo.model.TopicMessageAssociation;



public interface TopicMessageAssociationRepository extends CrudRepository<TopicMessageAssociation, Long> {


    List<TopicMessageAssociation> findByTopicId(Long topicId);

    List<TopicMessageAssociation> findByTopicIdOrderByPositionIndexAsc(Long topicId);

    Optional<TopicMessageAssociation> findByTopicIdAndMessageId(Long topicId, Long messageId);

    @Modifying
    @Query("DELETE FROM TopicMessageAssociation tma WHERE tma.topic.id = :topicId AND tma.message.id = :messageId")
    void deleteByTopicIdAndMessageId(Long topicId, Long messageId);

    // Get index position of message in topic
    @Query("SELECT tma.positionIndex FROM TopicMessageAssociation tma WHERE tma.topic.id = :topicId AND tma.message.id = :messageId")
    Optional<Integer> findPositionIndex(@Param("topicId") Long topicId, @Param("messageId") Long messageId);

    // Count the amount of message in a topic
    @Query("SELECT COUNT(tma) FROM TopicMessageAssociation tma WHERE tma.topic.id = :topicId")
    int countByTopicId(@Param("topicId") Long topicId);

    // Decrement index position of messages inside a topic starting from a specified position
    @Modifying
    @Query("UPDATE TopicMessageAssociation tma SET tma.positionIndex = tma.positionIndex - 1 WHERE tma.topic.id = :topicId AND tma.positionIndex > :start AND tma.positionIndex > 1")
    void decrementPositionsStartingFrom(@Param("topicId") Long topicId,
                                   @Param("start") int start);

    // Increment index position of messages inside a topic start from "start" to "end"
    @Modifying
    @Query("UPDATE TopicMessageAssociation tma SET tma.positionIndex = tma.positionIndex + 1 WHERE tma.topic.id = :topicId AND tma.positionIndex BETWEEN :start AND :end")
    void incrementPositionsBetween(@Param("topicId") Long topicId,
                                   @Param("start") int start,
                                   @Param("end") int end);

    // Decrement index position of messages inside a topic start from "start" to "end"
    @Modifying
    @Query("UPDATE TopicMessageAssociation tma SET tma.positionIndex = tma.positionIndex - 1 WHERE tma.topic.id = :topicId AND tma.positionIndex BETWEEN :start AND :end")
    void decrementPositionsBetween(@Param("topicId") Long topicId,
                                   @Param("start") int start,
                                   @Param("end") int end);
}
