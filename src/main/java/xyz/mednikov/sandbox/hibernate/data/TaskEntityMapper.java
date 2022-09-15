package xyz.mednikov.sandbox.hibernate.data;

import xyz.mednikov.sandbox.hibernate.model.Task;
import xyz.mednikov.sandbox.hibernate.model.TaskDTO;

import java.util.function.Function;

class TaskEntityMapper implements Function<TaskDTO, Task> {
  @Override
  public Task apply(TaskDTO taskDTO) {
    Task task = new Task();
    task.setId(taskDTO.id());
    task.setUserId(taskDTO.userId());
    task.setContent(taskDTO.content());
    task.setCompleted(taskDTO.completed());
    task.setCreatedAt(taskDTO.createdAt());
    return task;
  }
}
