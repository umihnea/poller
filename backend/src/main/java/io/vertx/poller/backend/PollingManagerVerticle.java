package io.vertx.poller.backend;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.poller.backend.history.IHistoryService;
import io.vertx.poller.backend.node.INodeService;
import io.vertx.poller.backend.node.Node;

import java.time.Instant;
import java.util.*;

public class PollingManagerVerticle extends AbstractVerticle {
  private static final int POLLING_FREQUENCY_IN_MS = 3000; // 3 seconds (in milliseconds)
  private static final int MAX_POOL_SIZE = 10;
  private static final Logger LOG = LoggerFactory.getLogger(PollingManagerVerticle.class);
  Map<Integer, Node> nodes;
  Deque<Integer> queue;
  Deque<JsonObject> payloads = new ArrayDeque<>();
  List<PollingVerticle> pollingVerticles;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    INodeService nodeService = INodeService.createProxy(vertx, "shogun.nodes.service");
    IHistoryService historyService = IHistoryService.createProxy(vertx, "shogun.history.service");

    // Handle responses from poller vertices
    EventBus eventBus = vertx.eventBus();
    eventBus.consumer("polling_manager.polling_finished", message -> this.handlePollResponse(message, historyService));

    // Handle registering and unregistering nodes on the fly
    eventBus.consumer("polling_manager.register_node", this::handleNodeRegistration);
    eventBus.consumer("polling_manager.unregister_node", this::handleNodeUnregistration);

    vertx.setPeriodic(POLLING_FREQUENCY_IN_MS, (_time) -> {
      while (!this.payloads.isEmpty()) {
        JsonObject payload = this.payloads.pop();
        vertx.eventBus().send("poller.poll", payload);
      }
    });

    this.loadExistingNodes(nodeService).onComplete(asyncResult -> {
      // Spawn the pool of polling vertices
      int initialSize = Math.max(this.nodes.size(), MAX_POOL_SIZE);
      this.pollingVerticles = new ArrayList<>();
      this.queue = new ArrayDeque<>();

      List<Future<Void>> deployments = new ArrayList<>();
      for (int index = 0; index < initialSize; index++) {
        deployments.add(this.deploymentFuture(new PollingVerticle()));
      }

      // After their successful deployment, send the first polling tasks
      CompositeFuture.all(new ArrayList<>(deployments)).onComplete(handler -> {
        for (Node node : this.nodes.values()) {
          this.query(node);
        }
      });
    }).onComplete(asyncResult -> {
      LOG.info("Manager started.");
      startPromise.complete();
    });
  }

  private void query(Node node) {
    JsonObject payload = node.toJson()
      .put("trace", UUID.randomUUID().toString())
      .put("emittedAt", Instant.now());

    // Instead of delivering the message directly, it
    // gets added to a queue of payloads which get
    // sent only when the periodic task reaches its
    // trigger. This helps add some delay in the system
    // and prevents being rate-limited.
    this.payloads.add(payload); // vertx.eventBus().send("poller.poll", payload);
  }

  private Future<Void> loadExistingNodes(INodeService nodeService) {
    this.nodes = new HashMap<>();

    Promise<Void> promise = Promise.promise();
    nodeService.all(asyncResult -> {
      if (asyncResult.failed()) {
        LOG.error("Failed to query existing nodes.");
        promise.fail(asyncResult.cause());
        return;
      }

      for (Node node : asyncResult.result()) {
        this.nodes.put(node.getId(), node);
      }
      promise.complete();
    });

    return promise.future();
  }

  private Future<Void> deploymentFuture(PollingVerticle pollingVerticle) {
    Promise<Void> promise = Promise.promise();
    vertx.deployVerticle(pollingVerticle, result -> {
      if (result.failed()) {
        LOG.error("Failed to deploy verticle.");
        promise.fail(result.cause());
        return;
      }

      promise.complete();
    });

    return promise.future();
  }

  private <T> void handlePollResponse(Message<T> message, IHistoryService historyService) {
    JsonObject pollData = (JsonObject) message.body();
    this.writeRecordToDatabase(pollData, historyService).compose(result -> this.pick())
      .onSuccess(this::query)
      .onFailure(LOG::error);
  }

  private Future<Void> writeRecordToDatabase(JsonObject pollData, IHistoryService historyService) {
    Promise<Void> promise = Promise.promise();
    LOG.info(pollData);

    historyService.logFromPollData(pollData, handler -> {
      if (handler.failed()) {
        promise.fail(handler.cause());
        return;
      }

      promise.complete();
    });

    return promise.future();
  }

  private Future<Node> pick() {
    if (this.queue.isEmpty()) {
      // Reinitialize queue with all nodes
      this.queue.addAll(this.nodes.keySet());
    }

    try {
      int nextId = this.queue.pop();
      Node node = this.nodes.get(nextId);
      return Future.succeededFuture(node);
    } catch (Exception e) {
      return Future.failedFuture(e);
    }
  }

  private <T> void handleNodeUnregistration(Message<T> message) {
    try {
      JsonObject request = (JsonObject) message.body();
      int nodeId = request.getInteger("nodeId");
      LOG.info("Received unregistration message. " + request);

      Deque<Integer> cleanedQueue = new ArrayDeque<>();
      while (!this.queue.isEmpty()) {
        Integer poppedId = this.queue.pop();
        if (poppedId != nodeId) {
          cleanedQueue.push(poppedId);
        }
      }

      this.queue = cleanedQueue;
      this.nodes.remove(nodeId);
      this.scaleWorkerPool();
    } catch (Exception e) {
      LOG.error(e);
    }
  }

  private <T> void handleNodeRegistration(Message<T> message) {
    try {
      JsonObject request = (JsonObject) message.body();
      Node createdNode = new Node(request);
      LOG.info("Received registration message. " + createdNode.toJson().toString());

      this.nodes.put(createdNode.getId(), createdNode);
      this.queue.add(createdNode.getId());
      this.scaleWorkerPool();
    } catch (Exception e) {
      LOG.error(e);
    }
  }

  private void scaleWorkerPool() {
    // todo: This is a stub function not implemented due to time constraints
    // but it would be a good idea to scale the worker pool based on the
    // number of existing nodes. I've written things on purpose such that
    // the scaling logic is decoupled from node handling such that vertices
    // do not have to be 1:1 with the nodes.
    LOG.info("Auto-scaling pool.");
  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception {
    super.stop(stopPromise);
  }
}
