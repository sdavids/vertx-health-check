package io.vertx.ext.healthchecks;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import org.junit.Test;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class AuthenticationTest extends HealthCheckTestBase {

  @Override
  AuthenticationProvider getAuthProvider() {
    return (jsonObject, handler) ->
      vertx.runOnContext(v -> {
        if ("admin".equals(jsonObject.getString("X-Username"))
          && "admin".equals(jsonObject.getString("X-Password"))) {
          handler.handle(Future.succeededFuture(User.create(new JsonObject().put("login", "admin"))));
        } else {
          handler.handle(Future.failedFuture("Not Authorized"));
        }
      });
  }

  @Test
  public void testAuthenticationFailed() {
    Restafari.get("/health")
      .then()
      .statusCode(403);
  }

  @Test
  public void testAuthenticationSuccessUsingHeader() {
    Restafari
      .given()
      .header("X-Username", "admin")
      .header("X-Password", "admin")
      .get("/health")
      .then()
      .statusCode(204);
  }

  @Test
  public void testAuthenticationFailedUsingHeader() {
    Restafari
      .given()
      .header("X-Username", "admin")
      .header("X-Password", "wrong password")
      .get("/health")
      .then()
      .statusCode(403);
  }

  @Test
  public void testAuthenticationSuccessfulUsingParam() {
    Restafari
      .given()
      .param("X-Username", "admin")
      .param("X-Password", "admin")
      .get("/health")
      .then()
      .statusCode(204);
  }

  @Test
  public void testAuthenticationFailedUsingParam() {
    Restafari
      .given()
      .param("X-Password", "admin")
      .get("/health")
      .then()
      .statusCode(403);
  }

  @Test
  public void testAuthenticationSuccessfulUsingForm() {
    Restafari
      .given()
      .formParam("X-Username", "admin")
      .formParam("X-Password", "admin")
      .post("/post-health")
      .then()
      .statusCode(204);
  }

  @Test
  public void testAuthenticationFailedUsingForm() {
    Restafari
      .given()
      .formParam("X-Username", "admin")
      .formParam("X-Password", "not my password")
      .post("/post-health")
      .then()
      .statusCode(403);
  }

  @Test
  public void testAuthenticationSuccessfulUsingBody() {
    Restafari
      .given()
      .body("{\"X-Username\":\"admin\", \"X-Password\":\"admin\"}")
      .header(CONTENT_TYPE, "application/json")
      .post("/post-health")
      .then()
      .statusCode(204);
  }

  @Test
  public void testAuthenticationFailedUsingBody() {
    Restafari
      .given()
      .body("{\"X-Username\":\"admin\", \"X-Password\":\"not my password\"}")
      .header(CONTENT_TYPE, "application/json")
      .post("/post-health")
      .then()
      .statusCode(403);
  }

  @Test
  public void testAuthenticationFailedUsingBodyBecauseOfMissingContentType() {
    Restafari
      .given()
      .body("{\"X-Username\":\"admin\", \"X-Password\":\"admin\"}")
      .post("/post-health")
      .then()
      .statusCode(403);
  }
}
