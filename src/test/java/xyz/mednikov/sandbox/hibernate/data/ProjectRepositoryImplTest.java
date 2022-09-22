package xyz.mednikov.sandbox.hibernate.data;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.cfg.Configuration;
import org.hibernate.reactive.provider.ReactiveServiceRegistryBuilder;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.service.ServiceRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import xyz.mednikov.sandbox.hibernate.model.Project;
import xyz.mednikov.sandbox.hibernate.model.ProjectDTO;
import xyz.mednikov.sandbox.hibernate.model.Task;

import java.util.Properties;

@ExtendWith(VertxExtension.class)
@Testcontainers
class ProjectRepositoryImplTest {

  private final String DB_NAME = "hibernatedb";
  private final String DB_USER = "user";
  private final String DB_PASSWORD = "secret";

  @Container
  PostgreSQLContainer container = new PostgreSQLContainer("postgres:13-alpine")
    .withDatabaseName(DB_NAME)
    .withUsername(DB_USER)
    .withPassword(DB_PASSWORD);

  ProjectRepositoryImpl repository;

  @BeforeEach
  void setup(Vertx vertx, VertxTestContext context){
    Properties hibernateProps = new Properties();
    String url = "jdbc:postgresql://localhost:" + Integer.toString(container.getFirstMappedPort()) + "/" + DB_NAME;
    hibernateProps.put("hibernate.connection.url", url);
    hibernateProps.put("hibernate.connection.username", DB_USER);
    hibernateProps.put("hibernate.connection.password", DB_PASSWORD);
    hibernateProps.put("javax.persistence.schema-generation.database.action", "create");
    hibernateProps.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL95Dialect");
    Configuration hibernateConfiguration = new Configuration();
    hibernateConfiguration.setProperties(hibernateProps);
    hibernateConfiguration.addAnnotatedClass(Task.class);
    hibernateConfiguration.addAnnotatedClass(Project.class);
    ServiceRegistry serviceRegistry = new ReactiveServiceRegistryBuilder()
      .applySettings(hibernateConfiguration.getProperties()).build();
    Stage.SessionFactory sessionFactory = hibernateConfiguration
      .buildSessionFactory(serviceRegistry).unwrap(Stage.SessionFactory.class);
    repository = new ProjectRepositoryImpl(sessionFactory);
    context.completeNow();
  }

  @Test
  void createProjectTest(Vertx vertx, VertxTestContext context){
    ProjectDTO projectDTO = new ProjectDTO(null, 1, "My project");
    context.verify(() -> {
      repository.createProject(projectDTO)
        .onFailure(err -> context.failNow(err))
        .onSuccess(result -> {
          Assertions.assertNotNull(result);
          Assertions.assertNotNull(result.id());
          Assertions.assertEquals(1, result.id());
          context.completeNow();
        });
    });
  }

  @Test
  void findProjectByIdDoesNotExistTest(Vertx vertx, VertxTestContext context){
    context.verify(() -> {
      repository.findProjectById(1)
        .onSuccess(r -> {
          Assertions.assertTrue(r.isEmpty());
          context.completeNow();
        })
        .onFailure(err -> context.failNow(err));
    });
  }

  @Test
  void findProjectByIdExistsTest(Vertx vertx, VertxTestContext context){
    ProjectDTO projectDTO = new ProjectDTO(null, 1, "My project");
    context.verify(() -> {
      repository.createProject(projectDTO)
        .compose(r -> repository.findProjectById(r.id()))
        .onFailure(err -> context.failNow(err))
        .onSuccess(result -> {
          Assertions.assertTrue(result.isPresent());
          context.completeNow();
        });
    });
  }

  @Test
  void removeProjectTest(Vertx vertx, VertxTestContext context){
    ProjectDTO projectDTO = new ProjectDTO(null, 1, "My project");
    context.verify(() -> {
      repository.createProject(projectDTO)
        .compose(r -> {
          Assertions.assertEquals(1, r.id());
          return repository.removeProject(r.id());
        }).compose(r -> repository.findProjectById(1))
        .onFailure(err -> context.failNow(err))
        .onSuccess(r -> {
          Assertions.assertTrue(r.isEmpty());
          context.completeNow();
        });
    });
  }

  @Test
  void updateProjectTest(Vertx vertx, VertxTestContext context){
    ProjectDTO projectDTO = new ProjectDTO(null, 1, "My project");
    context.verify(() -> {
      repository.createProject(projectDTO)
        .compose(r -> {
          Assertions.assertEquals(1, r.id());
          ProjectDTO updatedProject = new ProjectDTO(r.id(), r.userId(), "My updated project");
          return repository.updateProject(updatedProject);
        }).compose(r -> {
          Assertions.assertEquals("My updated project", r.name());
          return repository.findProjectById(1);
        }).onFailure(err -> context.failNow(err))
        .onSuccess(r ->{
          Assertions.assertTrue(r.isPresent());
          ProjectDTO result = r.get();
          Assertions.assertEquals("My updated project", result.name());
          context.completeNow();
        });
    });
  }

  @Test
  void findProjectsByUserTest(Vertx vertx, VertxTestContext context){
    ProjectDTO project1 = new ProjectDTO(null, 1, "My project");
    ProjectDTO project2 = new ProjectDTO(null, 1, "My project");
    ProjectDTO project3 = new ProjectDTO(null, 2, "My project");
    CompositeFuture createTasks = CompositeFuture.join(
      repository.createProject(project1),
      repository.createProject(project2),
      repository.createProject(project3)
    );
    context.verify(() -> {
      createTasks.compose(r -> {
          Assertions.assertTrue(r.succeeded());
          Assertions.assertTrue(r.isComplete());
          return repository.findProjectsByUser(1);
        }).onFailure(err -> context.failNow(err))
        .onSuccess(r -> {
          Assertions.assertEquals(2, r.projects().size());
          context.completeNow();
        });
    });
  }
}
