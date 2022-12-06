package xyz.mednikov.sandbox.hibernate.web;

import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.hibernate.cfg.Configuration;
import org.hibernate.reactive.provider.ReactiveServiceRegistryBuilder;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.service.ServiceRegistry;
import xyz.mednikov.sandbox.hibernate.data.ProjectRepository;
import xyz.mednikov.sandbox.hibernate.data.ProjectRepositoryImpl;
import xyz.mednikov.sandbox.hibernate.model.Project;
import xyz.mednikov.sandbox.hibernate.model.ProjectDTO;
import xyz.mednikov.sandbox.hibernate.model.Task;
import xyz.mednikov.sandbox.hibernate.service.SimpleProjectService;
import xyz.mednikov.sandbox.hibernate.service.SimpleProjectServiceImpl;

import java.util.Properties;

public class WebVerticle extends AbstractVerticle {

  private final SimpleProjectService projectService;

  public WebVerticle(SimpleProjectService projectService) {
    this.projectService = projectService;
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);

    router.route("/*").handler(BodyHandler.create());

    router.get("/projects/one/:id").handler(context -> {
      Integer id = Integer.valueOf(context.pathParam("id"));
      projectService.findProjectById(id)
        .onSuccess(result -> {
          if (result.isPresent()) {
            JsonObject body = JsonObject.mapFrom(result.get());
            context.response().setStatusCode(200).end(body.encode());
          } else {
            context.response().setStatusCode(404).end();
          }
        })
        .onFailure(err -> context.response().setStatusCode(500).end());
    });

    router.get("/projects/user/:userId").handler(context -> {
      Integer userId = Integer.valueOf(context.pathParam("userId"));
      projectService.findProjectsByUser(userId)
        .onSuccess(result -> {
          JsonObject body = JsonObject.mapFrom(result);
          context.response().setStatusCode(200).end(body.encode());
        })
        .onFailure(err -> context.response().setStatusCode(500).end());
    });

    router.delete("/projects/:id").handler(context -> {
      Integer id = Integer.valueOf(context.pathParam("id"));
      projectService.removeProject(id)
        .onSuccess(result -> context.response().setStatusCode(204).end())
        .onFailure(err -> context.response().setStatusCode(500).end());
    });

    router.post("/projects").handler(context -> {
      JsonObject body = context.getBodyAsJson();
      Integer userId = body.getInteger("userId");
      String name = body.getString("name");
      ProjectDTO payload = new ProjectDTO(null, userId, name);
      projectService.createProject(payload)
        .onSuccess(result -> {
          JsonObject responseBody = JsonObject.mapFrom(result);
          context.response().setStatusCode(201).end(responseBody.encode());
        })
        .onFailure(err -> context.response().setStatusCode(500).end());
    });

    router.put("/projects").handler(context -> {
      JsonObject body = context.getBodyAsJson();
      Integer id = body.getInteger("id");
      Integer userId = body.getInteger("userId");
      String name = body.getString("name");
      ProjectDTO payload = new ProjectDTO(id, userId, name);
      projectService.updateProject(payload)
        .onSuccess(result -> {
          JsonObject responseBody = JsonObject.mapFrom(result);
          context.response().setStatusCode(200).end(responseBody.encode());
        })
        .onFailure(err -> context.response().setStatusCode(500).end());
    });

    JsonObject config = config();
    Integer port = config.getInteger("port");
    server.requestHandler(router).listen(port).onSuccess(result -> startPromise.complete())
      .onFailure(err -> startPromise.fail(err));
  }

  public static void main(String[] args) {
    // 1. Hibernate configuration
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

    // 5. WebVerticle
    WebVerticle verticle = new WebVerticle(projectService);

    DeploymentOptions options = new DeploymentOptions();
    JsonObject config = new JsonObject();
    config.put("port", 8888);
    options.setConfig(config);

    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(verticle, options).onFailure(err -> err.printStackTrace())
      .onSuccess(res -> {
        System.out.println(res);
        System.out.println("Application is up and running");
      });
  }


}
