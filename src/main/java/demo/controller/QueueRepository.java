package demo.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import demo.model.Queue;
import demo.model.Message;

public interface QueueRepository extends CrudRepository<Queue, String> {

	// Find a queue by ID
	Optional<Queue> findById(String id);

	// Get all queues
	List<Queue> findAllBy();

	// Check if a queue exists by ID
	boolean existsById(String id);

	// Get all queues that contain a specific message
	List<Queue> findByMessagesContaining(Message message);

	// Get queues where the ID starts with a certain prefix
	List<Queue> findByIdStartingWith(String prefix);
}
