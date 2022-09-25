package xyz.mednikov.sandbox.hibernate.service;

import io.vertx.core.Future;
import xyz.mednikov.sandbox.hibernate.auth.NotOwnerException;
import xyz.mednikov.sandbox.hibernate.auth.Principal;
import xyz.mednikov.sandbox.hibernate.data.ProjectRepository;
import xyz.mednikov.sandbox.hibernate.model.ProjectDTO;
import xyz.mednikov.sandbox.hibernate.model.ProjectsList;

import java.util.Objects;
import java.util.Optional;

public record ProjectServiceImpl(ProjectRepository repository) implements ProjectService {
  @Override
  public Future<ProjectDTO> createProject(ProjectDTO projectDTO) {
    return repository.createProject(projectDTO);
  }

  @Override
  public Future<ProjectDTO> updateProject(Principal principal, ProjectDTO projectDTO) {
    Integer projectId = projectDTO.id();
    return repository.findProjectById(projectId).compose(result -> {
      if (result.isEmpty()) {
        return Future.failedFuture(new RuntimeException());
      }
      ProjectDTO project = result.get();
      if (Objects.equals(project.userId(), principal.userId())) {
        return repository.updateProject(projectDTO);
      } else {
        return Future.failedFuture(new NotOwnerException());
      }
    });
  }

  @Override
  public Future<Optional<ProjectDTO>> findProjectById(Integer id) {
    return repository.findProjectById(id);
  }

  @Override
  public Future<Void> removeProject(Principal principal, Integer id) {
    return repository.findProjectById(id).compose(result -> {
      if (result.isEmpty()) {
        return Future.failedFuture(new RuntimeException());
      }
      ProjectDTO project = result.get();
      if (Objects.equals(project.userId(), principal.userId())) {
        return repository.removeProject(id);
      } else {
        return Future.failedFuture(new NotOwnerException());
      }
    });
  }

  @Override
  public Future<ProjectsList> findProjectsByUser(Integer userId) {
    return repository.findProjectsByUser(userId);
  }
}
