package xyz.mednikov.sandbox.hibernate.data;

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
import xyz.mednikov.sandbox.hibernate.model.TaskDTO;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Properties;

@ExtendWith(VertxExtension.class)
@Testcontainers
class RelationshipsTest {


  private final String DB_NAME = "hibernatedb";
  private final String DB_USER = "user";
  private final String DB_PASSWORD = "secret";

  @Container
  PostgreSQLContainer container = new PostgreSQLContainer("postgres:13-alpine")
    .withDatabaseName(DB_NAME)
    .withUsername(DB_USER)
    .withPassword(DB_PASSWORD);

  TaskRepositoryImpl taskRepository;
  ProjectRepositoryImpl projectRepository;

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
    taskRepository = new TaskRepositoryImpl(sessionFactory);
    projectRepository = new ProjectRepositoryImpl(sessionFactory);
    context.completeNow();
  }


  @Test
  void createRelationshipTest(Vertx vertx, VertxTestContext context){
    ProjectDTO projectDTO = new ProjectDTO(null, 1, "My project");
    context.verify(() ->{
      projectRepository.createProject(projectDTO).compose(project->{
        Assertions.assertEquals(1, project.id());
        TaskDTO taskDTO = new TaskDTO(null, 1, "My task", false, LocalDateTime.now(), Optional.of(project));
        return taskRepository.createTask(taskDTO);
      }).onSuccess(result -> {
        Assertions.assertTrue(result.project().isPresent());
        context.completeNow();
      }).onFailure(err -> context.failNow(err));
    });
  }
}
