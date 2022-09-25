package xyz.mednikov.sandbox.hibernate.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.mednikov.sandbox.hibernate.auth.Principal;
import xyz.mednikov.sandbox.hibernate.data.ProjectRepository;
import xyz.mednikov.sandbox.hibernate.model.ProjectDTO;

import java.util.Optional;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

  @Mock private ProjectRepository repository;
  @InjectMocks private ProjectServiceImpl service;

  @Test
  void updateProjectIsOwnerTest(Vertx vertx, VertxTestContext context){
    ProjectDTO project = new ProjectDTO(1,1,"My project");
    Mockito.when(repository.findProjectById(1)).thenReturn(Future.succeededFuture(Optional.of(project)));
    Mockito.when(repository.updateProject(project)).thenReturn(Future.succeededFuture(project));
    Principal principal = new Principal(1);
    context.verify(() -> {
      service.updateProject(principal, project).onSuccess(result -> context.completeNow()).onFailure(err -> context.failNow(err));
    });
  }

  @Test
  void updateProjectIsNotOwnerTest(Vertx vertx, VertxTestContext context){
    ProjectDTO project = new ProjectDTO(1,1,"My project");
    Mockito.when(repository.findProjectById(1)).thenReturn(Future.succeededFuture(Optional.of(new ProjectDTO(1, 2, "My project"))));
    Principal principal = new Principal(1);
    context.verify(() -> {
      service.updateProject(principal, project).onSuccess(result -> context.failNow(new RuntimeException())).onFailure(err -> context.completeNow());
    });
  }

  @Test
  void removeProjectIsOwnerTest(Vertx vertx, VertxTestContext context){
    ProjectDTO project = new ProjectDTO(1,1,"My project");
    Mockito.when(repository.findProjectById(1)).thenReturn(Future.succeededFuture(Optional.of(project)));
    Mockito.when(repository.removeProject(1)).thenReturn(Future.succeededFuture());
    Principal principal = new Principal(1);
    context.verify(() -> {
      service.removeProject(principal, 1).onSuccess(result -> context.completeNow()).onFailure(err -> context.failNow(err));
    });
  }

  @Test
  void removeProjectIsNotOwnerTest(Vertx vertx, VertxTestContext context){
    ProjectDTO project = new ProjectDTO(1,1,"My project");
    Mockito.when(repository.findProjectById(1)).thenReturn(Future.succeededFuture(Optional.of(project)));
    Principal principal = new Principal(2);
    context.verify(() -> {
      service.removeProject(principal, 1).onSuccess(r -> context.failNow(new RuntimeException())).onFailure(err -> context.completeNow());
    });
  }
}
