package demo.repository;

import org.springframework.data.repository.CrudRepository;
import demo.model.Topic;

public interface TopicRepository extends CrudRepository<Topic, Long> {
}
