package xyz.mednikov.sandbox.hibernate.data;

import xyz.mednikov.sandbox.hibernate.model.Task;
import xyz.mednikov.sandbox.hibernate.model.TaskDTO;

import java.util.function.Function;

class TaskDTOMapper implements Function<Task, TaskDTO> {
  @Override
  public TaskDTO apply(Task task) {
    return new TaskDTO(task.getId(), task.getUserId(), task.getContent(), task.isCompleted(), task.getCreatedAt());
  }
}
