package xyz.mednikov.sandbox.hibernate.service;

import io.vertx.core.Future;
import xyz.mednikov.sandbox.hibernate.data.ProjectRepository;
import xyz.mednikov.sandbox.hibernate.model.ProjectDTO;
import xyz.mednikov.sandbox.hibernate.model.ProjectsList;

import java.util.Optional;

public record SimpleProjectServiceImpl(ProjectRepository repository) implements SimpleProjectService {
  @Override
  public Future<ProjectDTO> createProject(ProjectDTO projectDTO) {
    return repository().createProject(projectDTO);
  }

  @Override
  public Future<ProjectDTO> updateProject(ProjectDTO projectDTO) {
    return repository().updateProject(projectDTO);
  }

  @Override
  public Future<Optional<ProjectDTO>> findProjectById(Integer id) {
    return repository().findProjectById(id);
  }

  @Override
  public Future<Void> removeProject(Integer id) {
    return repository().removeProject(id);
  }

  @Override
  public Future<ProjectsList> findProjectsByUser(Integer userId) {
    return repository().findProjectsByUser(userId);
  }
}
