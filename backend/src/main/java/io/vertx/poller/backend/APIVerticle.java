package io.vertx.poller.backend;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.poller.backend.history.IHistoryService;
import io.vertx.poller.backend.history.Record;
import io.vertx.poller.backend.node.INodeService;
import io.vertx.poller.backend.node.Node;

import java.util.List;

public class APIVerticle extends AbstractVerticle {
  private static final Logger LOG = LoggerFactory.getLogger(APIVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router router = Router.router(vertx);
    INodeService nodeService = INodeService.createProxy(vertx, "shogun.nodes.service");
    IHistoryService historyService = IHistoryService.createProxy(vertx, "shogun.history.service");

    router.get("/api/nodes").handler(context -> {
      nodeService.all(result -> {
        if (result.failed()) {
          context.response().setStatusCode(400).end();
          return;
        }

        List<Node> nodes = result.result();
        JsonArray jsonArray = new JsonArray();
        nodes.stream().map(Node::toJson).forEach(jsonArray::add);
        context.response().putHeader("content-type", "application/json")
          .setStatusCode(200)
          .end(jsonArray.encode());
      });
    });

    router.post("/api/node").handler(BodyHandler.create()).handler(context -> {
      JsonObject body = context.getBodyAsJson();
      nodeService.create(body, result -> {
        if (result.failed()) {
          context.response().setStatusCode(400).end();
          return;
        }

        JsonObject createdObject = result.result();
        context.response().putHeader("content-type", "application/json")
          .setStatusCode(201) // http 201 created
          .end(createdObject.encode());
      });
    });

    router.delete("/api/node/:node_id").handler(context -> {
      int nodeId = Integer.parseInt(context.request().getParam("node_id"));
      nodeService.delete(nodeId, result -> {
        if (result.failed()) {
          context.response().setStatusCode(400).end();
          return;
        }

        context.response().setStatusCode(200).end();
      });
    });

    router.put("/api/node/:node_id").handler(BodyHandler.create()).handler(context -> {
      int nodeId = Integer.parseInt(context.request().getParam("node_id"));
      JsonObject body = context.getBodyAsJson();
      nodeService.update(nodeId, body, result -> {
        if (result.failed()) {
          context.response().setStatusCode(400).end();
          return;
        }

        context.response().setStatusCode(200).end();
      });
    });

    router.post("/api/history").handler(BodyHandler.create()).handler(context -> {
      JsonObject body = context.getBodyAsJson();
      // Service proxy methods do not accept Instant as parameters
      // so, we pass the entire JSON body and convert to Instant
      // internally.
      historyService.recordsInInterval(body, result -> {
        if (result.failed()) {
          context.response().setStatusCode(400).end();
          return;
        }

        List<Record> records = result.result();
        JsonArray jsonArray = new JsonArray();
        records.stream().map(Record::toJson).forEach(jsonArray::add);
        context.response().putHeader("content-type", "application/json")
          .setStatusCode(200)
          .end(jsonArray.encode());
      });
    });

    router.route().failureHandler(LOG::error);
    vertx.createHttpServer().requestHandler(router).listen(8080);
    LOG.info("Deployed API Verticle.");
  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {
    super.stop(stopPromise);
  }
}
