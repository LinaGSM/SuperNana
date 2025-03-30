package demo.repository;

import demo.model.ArchivedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArchivedMessageRepository extends JpaRepository<ArchivedMessage, Long> {
    List<ArchivedMessage> findByOriginalQueueId(String queueId);
    List<ArchivedMessage> findByOriginalContentContaining(String keyword);
    List<ArchivedMessage> findAll();
}