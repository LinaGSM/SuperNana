package demo.controller;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import demo.model.Message;
import demo.model.MessageQueue;

public interface MessageRepository extends CrudRepository<Message, Long> {

	Message findById(long id);

	List<Message> findAllByQueue(MessageQueue queue);

	List<Message> findAllByQueueOrderByIdAsc(MessageQueue queue); // FIFO retrieval
	List<Message> findByIdGreaterThanEqual(Long startId); // Retrieve messages from a given ID
	List<Message> findByTextContaining(String keyword); // Search messages by content
}
