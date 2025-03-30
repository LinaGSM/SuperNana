package demo.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import demo.model.Message;
import demo.model.Queue;

public interface MessageRepository extends CrudRepository<Message, Long> {

	Message findById(long id);

	List<Message> findAllByQueue(Queue queue);

	List<Message> findAllByQueueOrderByIdAsc(Queue queue); // FIFO retrieval
	List<Message> findByIdGreaterThanEqual(Long startId); // Retrieve messages from a given ID
	List<Message> findByTextContaining(String keyword); // Search messages by content
}
