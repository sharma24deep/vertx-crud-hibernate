package xyz.mednikov.sandbox.hibernate.data;

import xyz.mednikov.sandbox.hibernate.model.Project;
import xyz.mednikov.sandbox.hibernate.model.ProjectDTO;

import java.util.function.Function;

class ProjectEntityMapper implements Function<ProjectDTO, Project> {

  @Override
  public Project apply(ProjectDTO projectDTO) {
    Project entity = new Project();
    entity.setId(projectDTO.id());
    entity.setUserId(projectDTO.userId());
    entity.setName(projectDTO.name());
    return entity;
  }
}
