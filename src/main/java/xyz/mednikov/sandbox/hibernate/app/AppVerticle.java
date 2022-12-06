package xyz.mednikov.sandbox.hibernate.app;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.hibernate.cfg.Configuration;
import org.hibernate.reactive.provider.ReactiveServiceRegistryBuilder;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.service.ServiceRegistry;
import xyz.mednikov.sandbox.hibernate.data.ProjectRepository;
import xyz.mednikov.sandbox.hibernate.data.ProjectRepositoryImpl;
import xyz.mednikov.sandbox.hibernate.model.Project;
import xyz.mednikov.sandbox.hibernate.model.Task;
import xyz.mednikov.sandbox.hibernate.service.SimpleProjectService;
import xyz.mednikov.sandbox.hibernate.service.SimpleProjectServiceImpl;
import xyz.mednikov.sandbox.hibernate.web.WebVerticle;

import java.util.Properties;

public class AppVerticle extends AbstractVerticle {

  private final SimpleProjectService projectService;

  public AppVerticle(SimpleProjectService projectService){
    this.projectService = projectService;
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    WebVerticle webVerticle = new WebVerticle(projectService);
    DeploymentOptions options = new DeploymentOptions();
    JsonObject config = new JsonObject();
    config.put("port", 8888);
    options.setConfig(config);
    vertx.deployVerticle(webVerticle, options)
      .onFailure(err->startPromise.fail(err))
      .onSuccess(res -> startPromise.complete());
  }

  public static void main(String[] args) {
    Properties hibernateProps = new Properties();
    String url = "jdbc:postgresql://localhost:5432/hibernatedb";
    hibernateProps.put("hibernate.connection.url", url);
    hibernateProps.put("hibernate.connection.username", "user");
    hibernateProps.put("hibernate.connection.password", "usersecret");
    hibernateProps.put("javax.persistence.schema-generation.database.action", "create");
    hibernateProps.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL95Dialect");
    Configuration hibernateConfiguration = new Configuration();
    hibernateConfiguration.setProperties(hibernateProps);
    hibernateConfiguration.addAnnotatedClass(Task.class);
    hibernateConfiguration.addAnnotatedClass(Project.class);

    // 2. Session factroy
    ServiceRegistry serviceRegistry = new ReactiveServiceRegistryBuilder()
      .applySettings(hibernateConfiguration.getProperties()).build();
    Stage.SessionFactory sessionFactory = hibernateConfiguration
      .buildSessionFactory(serviceRegistry).unwrap(Stage.SessionFactory.class);

    // 3. Project repository
    ProjectRepository projectRepository = new ProjectRepositoryImpl(sessionFactory);

    // 4. Project service
    SimpleProjectService projectService = new SimpleProjectServiceImpl(projectRepository);

    AppVerticle verticle = new AppVerticle(projectService);

    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(verticle)
      .onSuccess(res -> {
        System.out.println("The app is up and running");
      })
      .onFailure(err -> err.printStackTrace());

  }
}
