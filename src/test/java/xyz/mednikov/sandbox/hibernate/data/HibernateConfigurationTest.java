package xyz.mednikov.sandbox.hibernate.data;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.cfg.Configuration;
import org.hibernate.reactive.provider.ReactiveServiceRegistryBuilder;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.service.ServiceRegistry;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import xyz.mednikov.sandbox.hibernate.model.Task;

import java.time.LocalDateTime;
import java.util.Properties;
import java.util.concurrent.CompletionStage;

@ExtendWith(VertxExtension.class)
@Disabled
class HibernateConfigurationTest {

  @Test
  void initializeHibernateWithCodeTest(Vertx vertx, VertxTestContext context){
    // 1. Create Properties with config data
    Properties hibernateProps = new Properties();

    //url
    hibernateProps.put("hibernate.connection.url", "jdbc:postgresql://localhost:5432/hdemodb");
    //credentials
    hibernateProps.put("hibernate.connection.username", "hdemouser");
    hibernateProps.put("hibernate.connection.password", "secret");

    // schema generation
    hibernateProps.put("javax.persistence.schema-generation.database.action", "create");

    // dialect *
    hibernateProps.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL95Dialect");

    // 2. Create Hibernate Configuration
    Configuration hibernateConfiguration = new Configuration();
    hibernateConfiguration.setProperties(hibernateProps);
    hibernateConfiguration.addAnnotatedClass(Task.class);

    // 3. Create ServiceRegistry
    ServiceRegistry serviceRegistry = new ReactiveServiceRegistryBuilder()
      .applySettings(hibernateConfiguration.getProperties()).build();

    // 4. Create SessionFactory
    Stage.SessionFactory sessionFactory = hibernateConfiguration
      .buildSessionFactory(serviceRegistry).unwrap(Stage.SessionFactory.class);

    // Do something with db
    Task task = new Task();
    task.setContent("Hello this is new task");
    task.setCompleted(false);
    task.setUserId(1);
    task.setCreatedAt(LocalDateTime.now());

    System.out.println("Task ID before insertion is: " + task.getId());

    CompletionStage<Void> insertionResult = sessionFactory.withTransaction((s, t) -> s.persist(task));

    Future<Void> future = Future.fromCompletionStage(insertionResult);
    context.verify(() -> future.onFailure(err -> context.failNow(err)).onSuccess(r -> {
      System.out.println("Task ID after insertion is: " + task.getId());
      context.completeNow();
    }));

  }
}
