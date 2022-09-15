package xyz.mednikov.sandbox.hibernate.data;

import io.vertx.core.Future;
import xyz.mednikov.sandbox.hibernate.model.TaskDTO;
import xyz.mednikov.sandbox.hibernate.model.TasksList;

import java.util.Optional;

public interface TaskRepository {

  Future<TaskDTO> createTask (TaskDTO task);

  Future<TaskDTO> updateTask (TaskDTO task);

  Future<Void> removeTask (Integer id);

  Future<Optional<TaskDTO>> findTaskById (Integer id);

  Future<TasksList> findTasksByUser (Integer userId);
}
