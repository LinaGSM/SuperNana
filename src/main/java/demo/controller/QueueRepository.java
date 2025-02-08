package demo.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.repository.CrudRepository;
import demo.model.MessageQueue;

public interface QueueRepository extends CrudRepository<MessageQueue, String> {

	Optional<MessageQueue> findById(String id);

	List<MessageQueue> findAllBy();
}