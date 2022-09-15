package xyz.mednikov.sandbox.hibernate.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class TestcontainersTest {

  @Container
  PostgreSQLContainer container = new PostgreSQLContainer("postgres:13-alpine")
    .withDatabaseName("testcontainersdb")
    .withUsername("tcuser")
    .withPassword("tcsecret");

  @Test
  void testContainersIsRunningTest(){
    Assertions.assertTrue(container.isCreated());
    Assertions.assertTrue(container.isRunning());
  }
}
