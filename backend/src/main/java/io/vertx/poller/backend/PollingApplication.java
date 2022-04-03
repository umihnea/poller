package io.vertx.poller.backend;

import io.vertx.core.Vertx;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.poller.backend.history.HistoryService;
import io.vertx.poller.backend.history.IHistoryService;
import io.vertx.poller.backend.history.RecordRepository;
import io.vertx.poller.backend.node.INodeService;
import io.vertx.poller.backend.node.NodeRepository;
import io.vertx.poller.backend.node.NodeService;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.sqlclient.PoolOptions;

public class PollingApplication {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    // todo: stash in .env file
    MySQLConnectOptions connectOptions = new MySQLConnectOptions().setPort(3306)
      .setHost("localhost")
      .setDatabase("poller")
      .setUser("mihnea")
      .setPassword("12island21")
      .setTracingPolicy(TracingPolicy.ALWAYS);
    MySQLPool client = MySQLPool.pool(vertx, connectOptions, new PoolOptions().setMaxSize(5));

    NodeRepository nodeRepository = new NodeRepository(client);
    INodeService nodeService = new NodeService(nodeRepository);
    new ServiceBinder(vertx).setAddress("shogun.nodes.service").register(INodeService.class, nodeService);

    RecordRepository recordRepository = new RecordRepository(client);
    IHistoryService historyService = new HistoryService(recordRepository);
    new ServiceBinder(vertx).setAddress("shogun.history.service").register(IHistoryService.class, historyService);

    // The polling manager oversees a pool of polling verticles.
    // The polling verticles are the workers who poll nodes and
    // provide reports.
    vertx.deployVerticle(new PollingManagerVerticle());

    vertx.deployVerticle(new APIVerticle());
  }
}
