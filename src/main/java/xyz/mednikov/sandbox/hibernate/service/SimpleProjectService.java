package xyz.mednikov.sandbox.hibernate.service;

import io.vertx.core.Future;
import xyz.mednikov.sandbox.hibernate.model.ProjectDTO;
import xyz.mednikov.sandbox.hibernate.model.ProjectsList;

import java.util.Optional;

public interface SimpleProjectService {

  Future<ProjectDTO> createProject (ProjectDTO projectDTO);

  Future<ProjectDTO> updateProject(ProjectDTO projectDTO);

  Future<Optional<ProjectDTO>> findProjectById (Integer id);

  Future<Void> removeProject (Integer id);

  Future<ProjectsList> findProjectsByUser (Integer userId);
}
