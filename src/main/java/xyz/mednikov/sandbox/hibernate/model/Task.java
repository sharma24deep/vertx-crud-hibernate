package xyz.mednikov.sandbox.hibernate.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name="tasks")
public class Task {

  @Id @GeneratedValue
  private Integer id;
  private Integer userId;
  private String content;
  private boolean completed;
  private LocalDateTime createdAt;

  @ManyToOne @JoinColumn(name="projectId", nullable=true)
  private Project project;

  public Task(){

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

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Task task = (Task) o;
    return id.equals(task.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, content, completed, createdAt);
  }
}
