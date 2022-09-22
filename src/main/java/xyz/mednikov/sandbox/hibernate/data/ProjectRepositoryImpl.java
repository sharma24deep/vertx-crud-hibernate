package xyz.mednikov.sandbox.hibernate.data;

import io.vertx.core.Future;
import org.hibernate.reactive.stage.Stage;
import xyz.mednikov.sandbox.hibernate.model.*;

import javax.persistence.criteria.*;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public record ProjectRepositoryImpl(Stage.SessionFactory sessionFactory) implements ProjectRepository {
  @Override
  public Future<ProjectDTO> createProject(ProjectDTO projectDTO) {
    ProjectEntityMapper entityMapper = new ProjectEntityMapper();
    Project entity = entityMapper.apply(projectDTO);
    CompletionStage<Void> result = sessionFactory.withTransaction((s, t) -> s.persist(entity));
    ProjectDTOMapper dtoMapper = new ProjectDTOMapper();
    Future<ProjectDTO> future = Future.fromCompletionStage(result).map(v -> dtoMapper.apply(entity));
    return future;
  }

  @Override
  public Future<ProjectDTO> updateProject(ProjectDTO projectDTO) {
    CriteriaBuilder criteriaBuilder = sessionFactory.getCriteriaBuilder();
    CriteriaUpdate<Project> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(Project.class);
    Root<Project> root = criteriaUpdate.from(Project.class);
    Predicate predicate = criteriaBuilder.equal(root.get("id"), projectDTO.id());

    criteriaUpdate.set("name", projectDTO.name());

    criteriaUpdate.where(predicate);

    CompletionStage<Integer> result = sessionFactory.withTransaction((s, t) -> s.createQuery(criteriaUpdate).executeUpdate());
    Future<ProjectDTO> future = Future.fromCompletionStage(result).map(r -> projectDTO);
    return future;
  }

  @Override
  public Future<Optional<ProjectDTO>> findProjectById(Integer id) {
    ProjectDTOMapper mapper = new ProjectDTOMapper();
    CompletionStage<Project> result = sessionFactory().withTransaction((s,t) -> s.find(Project.class, id));
    Future<Optional<ProjectDTO>> future = Future.fromCompletionStage(result)
      .map(r -> Optional.ofNullable(r))
      .map(r -> r.map(mapper));
    return future;
  }

  @Override
  public Future<Void> removeProject(Integer id) {
    CriteriaBuilder criteriaBuilder = sessionFactory.getCriteriaBuilder();
    CriteriaDelete<Project> criteriaDelete = criteriaBuilder.createCriteriaDelete(Project.class);
    Root<Project> root = criteriaDelete.from(Project.class);
    Predicate predicate = criteriaBuilder.equal(root.get("id"), id);
    criteriaDelete.where(predicate);

    CompletionStage<Integer> result = sessionFactory.withTransaction((s,t) -> s.createQuery(criteriaDelete).executeUpdate());
    Future<Void> future = Future.fromCompletionStage(result).compose(r -> Future.succeededFuture());
    return future;
  }

  @Override
  public Future<ProjectsList> findProjectsByUser(Integer userId) {
    ProjectDTOMapper dtoMapper = new ProjectDTOMapper();
    CriteriaBuilder criteriaBuilder = sessionFactory.getCriteriaBuilder();
    CriteriaQuery<Project> criteriaQuery = criteriaBuilder.createQuery(Project.class);
    Root<Project> root = criteriaQuery.from(Project.class);
    Predicate predicate = criteriaBuilder.equal(root.get("userId"), userId);
    criteriaQuery.where(predicate);
    CompletionStage<List<Project>> result = sessionFactory().withTransaction((s, t) -> s.createQuery(criteriaQuery).getResultList());
    Future<ProjectsList> future = Future.fromCompletionStage(result)
      .map(list -> list.stream().map(dtoMapper).collect(Collectors.toList()))
      .map(list -> new ProjectsList(list));
    return future;
  }
}
