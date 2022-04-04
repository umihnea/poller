package io.vertx.poller.backend;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.poller.backend.node.Node;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;

public class PollingVerticle extends AbstractVerticle {
  private static final Logger LOG = LoggerFactory.getLogger(PollingVerticle.class);
  private static final long REQUEST_TIMEOUT_MILLISECONDS = 5000;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    WebClient client = WebClient.create(vertx, new WebClientOptions().setKeepAlive(true).setMaxPoolSize(100));

    EventBus eventBus = vertx.eventBus();
    eventBus.consumer("poller.poll", message -> {
      JsonObject requestData = (JsonObject) message.body();

      String traceId = requestData.getString("trace");
      Instant emittedAt = requestData.getInstant("emittedAt");
      requestData.remove("trace");
      requestData.remove("emittedAt");

      Node node = new Node(requestData);
      this.pollNode(client, node)
        .onComplete(result -> {
          JsonObject responseData = new JsonObject();
          if (result.failed()) {
            responseData.mergeIn(this.packFailedPoll(node, result.cause()));
          } else {
            responseData = result.result();
          }

          responseData.put("trace", traceId).put("emittedAt", emittedAt);
          eventBus.send("polling_manager.polling_finished", responseData);
        });
    });

    LOG.info("Polling verticle ready to work.");
    startPromise.complete();
  }

  private JsonObject packFailedPoll(Node node, Throwable cause) {
    return new JsonObject()
      .put("nodeId", node.getId())
      .putNull("status")
      .put("cause", cause.toString())
      .put("finishedAt", Instant.now());
  }

  private Future<JsonObject> packRecord(int id, int code) {
    return Future.succeededFuture(
      new JsonObject().put("nodeId", id).put("status", code).put("finishedAt", Instant.now())
    );
  }

  private Future<JsonObject> pollNode(WebClient client, Node node) {
    return this.parseUrl(node.getUrl()).compose(
      components -> client.get(
        components.getInteger("port"),
        components.getString("host"),
        components.getString("path")
      ).timeout(REQUEST_TIMEOUT_MILLISECONDS).send()
    ).compose(bufferHttpResponse -> {
      int code = bufferHttpResponse.statusCode();
      return this.packRecord(node.getId(), code);
    }).onFailure(LOG::error);
  }

  private Future<JsonObject> parseUrl(String url) {
    try {
      URL parsedUrl = new URL(url);
      int port = parsedUrl.getPort() != -1 ? parsedUrl.getPort() : parsedUrl.getDefaultPort();
      return Future.succeededFuture(new JsonObject()
        .put("port", port)
        .put("host", parsedUrl.getHost())
        .put("path", parsedUrl.getPath() + "?" + parsedUrl.getQuery())
      );
    } catch (MalformedURLException e) {
      return Future.failedFuture(e);
    }
  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {
    super.stop(stopPromise);
  }
}
