package demo.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import demo.model.MessageQueue;
import demo.model.Message;

public interface QueueRepository extends CrudRepository<MessageQueue, String> {

	// Find a queue by ID
	Optional<MessageQueue> findById(String id);

	// Get all queues
	List<MessageQueue> findAllBy();

	// Check if a queue exists by ID
	boolean existsById(String id);

	// Get all queues that contain a specific message
	List<MessageQueue> findByMessagesContaining(Message message);

	// Get queues where the ID starts with a certain prefix
	List<MessageQueue> findByIdStartingWith(String prefix);
}
