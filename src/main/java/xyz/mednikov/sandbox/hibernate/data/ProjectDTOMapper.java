package xyz.mednikov.sandbox.hibernate.data;

import xyz.mednikov.sandbox.hibernate.model.Project;
import xyz.mednikov.sandbox.hibernate.model.ProjectDTO;

import java.util.function.Function;

class ProjectDTOMapper implements Function<Project, ProjectDTO> {

  @Override
  public ProjectDTO apply(Project project) {
    return new ProjectDTO(project.getId(), project.getUserId(), project.getName());
  }
}
