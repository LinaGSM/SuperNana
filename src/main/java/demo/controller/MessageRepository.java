package demo.controller;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import demo.model.Message;
import demo.model.MessageQueue;

public interface MessageRepository extends CrudRepository<Message, Long> {

	// List<Message> findByLastName(String lastName);

	Message findById(long id);

	List<Message> findAllByQueue(MessageQueue queue);
}