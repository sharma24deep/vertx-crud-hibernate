package xyz.mednikov.sandbox.hibernate.model;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name="projects")
public class Project {

  @Id @GeneratedValue
  private Integer id;

  private Integer userId;

  private String name;

  @OneToMany(mappedBy = "project")
  private List<Task> tasks;

  public Project(){

  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Task> getTasks() {
    return tasks;
  }

  public void setTasks(List<Task> tasks) {
    this.tasks = tasks;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Project project = (Project) o;
    return id.equals(project.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, name);
  }
}
