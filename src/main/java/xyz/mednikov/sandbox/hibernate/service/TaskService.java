package xyz.mednikov.sandbox.hibernate.service;

import io.vertx.core.Future;
import xyz.mednikov.sandbox.hibernate.model.TaskDTO;
import xyz.mednikov.sandbox.hibernate.model.TasksList;

import java.util.Optional;

public interface TaskService {

  Future<TaskDTO> createTask (TaskDTO task);

  Future<TaskDTO> updateTask(TaskDTO task);

  Future<Optional<TaskDTO>> findTaskById (Integer id);

  Future<Void> removeTask (Integer id);

  Future<TasksList> findTasksByUser (Integer userId);
}
