package io.vertx.poller.backend.node;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class NodeService implements INodeService {
  private static final Logger LOG = LoggerFactory.getLogger(NodeService.class);
  NodeRepository repository;

  public NodeService(NodeRepository repository) {
    this.repository = repository;
  }

  @Override
  public void all(Handler<AsyncResult<List<Node>>> handler) {
    this.repository.all(result -> {
      if (result.failed()) {
        LOG.error("Query failed in repository.");
        handler.handle(Future.failedFuture(result.cause()));
        return;
      }

      List<Node> nodes = new ArrayList<>(result.result());
      handler.handle(Future.succeededFuture(nodes));
    });
  }

  @Override
  public void create(JsonObject data, Handler<AsyncResult<JsonObject>> handler) {
    // Assume data is valid because validation is handled externally
    Node node = new Node(
      // Whatever we set the ID to does not matter right
      // now since the repository method will override it
      // anyway.
      -1,
      data.getString("name"),
      data.getString("url"),
      data.getInstant("createdAt")
    );

    this.repository.create(node, asyncNode -> {
      if (asyncNode.failed()) {
        handler.handle(Future.failedFuture(asyncNode.cause()));
        return;
      }

      Node insertedNode = asyncNode.result();
      handler.handle(Future.succeededFuture(insertedNode.toJson()));
    });
  }

  @Override
  public void delete(Integer nodeId, Handler<AsyncResult<Void>> handler) {
    this.repository.delete(nodeId, asyncResult -> {
      if (asyncResult.failed()) {
        handler.handle(Future.failedFuture(asyncResult.cause()));
        return;
      }

      handler.handle(Future.succeededFuture());
    });
  }

  @Override
  public void update(Integer nodeId, JsonObject data, Handler<AsyncResult<Void>> handler) {
    this.repository.findOne(nodeId)
      .compose(existingNode -> this.computeUpdatedNode(existingNode, data))
      .onComplete(updatedNodeResult -> this.commitUpdate(updatedNodeResult, handler));
  }

  private void commitUpdate(AsyncResult<Node> updatedNodeResult, Handler<AsyncResult<Void>> handler) {
    if (updatedNodeResult.failed()) {
      LOG.error("Commit update failed.");
      handler.handle(Future.failedFuture(updatedNodeResult.cause()));
      return;
    }

    Node updatedNode = updatedNodeResult.result();
    LOG.info("Updated node: " + updatedNode);
    this.repository.update(updatedNode.getId(), updatedNode, asyncResult -> {
      if (asyncResult.failed()) {
        handler.handle(Future.failedFuture(asyncResult.cause()));
        return;
      }

      handler.handle(Future.succeededFuture());
    });
  }

  private Future<Node> computeUpdatedNode(Node existingNode, JsonObject data) {
    try {
      Node updatedNode = new Node(
        existingNode.getId(),
        data.getString("name", existingNode.getName()),
        data.getString("url", existingNode.getUrl()),
        existingNode.getCreatedAt()
      );

      return Future.succeededFuture(updatedNode);
    } catch (Exception e) {
      LOG.error(e);
      return Future.failedFuture(e);
    }
  }
}
