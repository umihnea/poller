package io.vertx.poller.backend.node;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.poller.backend.datetime.TimeConvertor;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class NodeRepository {
  private static final Logger LOG = LoggerFactory.getLogger(NodeRepository.class);
  MySQLPool pooledClient;

  public NodeRepository(MySQLPool pooledClient) {
    this.pooledClient = pooledClient;
  }

  public void create(Node node, Handler<AsyncResult<Node>> handler) {
    Tuple attributes = Tuple.of(
      node.getName(),
      node.getUrl(),
      TimeConvertor.databaseTimestampFromInstant(node.getCreatedAt())
    );

    this.pooledClient
      .preparedQuery("insert into node (name, url, created_at) values (?, ?, ?);")
      .execute(attributes)
      .compose(rows -> this.queryLastInsertedId(rows, attributes))
      .onComplete(lastInsertedIdResult -> {
        if (lastInsertedIdResult.failed()) {
          LOG.error("Could not retrieve last inserted ID.", lastInsertedIdResult.cause());
          handler.handle(Future.failedFuture(
              new Throwable("Could not retrieve last inserted ID.")
            )
          );

          return;
        }

        Node nodeWithId = new Node(node.getId(), node.getName(), node.getUrl(), node.getCreatedAt());
        nodeWithId.setId(lastInsertedIdResult.result());
        handler.handle(Future.succeededFuture(nodeWithId));
      });
  }

  private Future<Integer> queryLastInsertedId(RowSet<Row> insertedRows, Tuple attributes) {
    if (insertedRows.rowCount() == 0) {
      LOG.error("Failed insert statement.");
      return Future.failedFuture("Failed insert statement.");
    }

    Tuple slicedAttributes = Tuple.of(
      attributes.get(String.class, 0),
      attributes.get(String.class, 1)
    );

    return this.pooledClient
      // select last_insert_id() is hard to parse and is too much work
      // and unreliable in the case of multiple writers anyway
      .preparedQuery("select id from node where name=? and url=? order by id desc limit 1;")
      .execute(slicedAttributes).compose(
        rows -> {
          Integer lastId = rows.iterator().next().getInteger("id");
          return Future.succeededFuture(lastId);
        }
      );
  }

  public void delete(Integer nodeId, Handler<AsyncResult<Void>> handler) {
    this.pooledClient.preparedQuery("delete from node where id=?;")
      .execute(Tuple.of(nodeId))
      .onComplete(result -> {
        if (result.failed()) {
          LOG.error("Failed due to: " + result.cause());
          handler.handle(Future.failedFuture(result.cause()));
        } else if (result.result().rowCount() == 0) {
          LOG.error("Deleted 0 rows.");
          handler.handle(Future.failedFuture("Deleted 0 rows."));
        }

        handler.handle(Future.succeededFuture());
      });
  }

  public void all(Handler<AsyncResult<List<Node>>> handler) {
    this.pooledClient
      .query("select * from node;")
      .execute(ar -> {
        if (ar.succeeded()) {
          RowSet<Row> rows = ar.result();

          try {
            List<Node> nodes = new ArrayList<>();
            for (Row row : rows) {
              nodes.add(this.rowToEntity(row));
            }

            handler.handle(Future.succeededFuture(nodes));
          } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            handler.handle(Future.failedFuture(e));
          }
        } else {
          LOG.error(ar.cause().getMessage());
          handler.handle(Future.failedFuture(ar.cause()));
        }
      });
  }

  public Future<Node> findOne(Integer nodeId) {
    return this.pooledClient
      .preparedQuery("select * from node where id=? limit 1;")
      .execute(Tuple.of(nodeId))
      .compose(rows -> {
        try {
          Row row = rows.iterator().next();
          Node node = this.rowToEntity(row);
          return Future.succeededFuture(node);
        } catch (NoSuchElementException e) {
          LOG.error(String.format("No element matching (id = %d) found.", nodeId));
          return Future.failedFuture(e);
        } catch (Exception e) {
          LOG.error(e);
          return Future.failedFuture(e);
        }
      });
  }

  public void update(Integer nodeId, Node updatedNode, Handler<AsyncResult<Void>> handler) {
    this.pooledClient.preparedQuery("update node set name=?, url=? where id=?;")
      .execute(Tuple.of(updatedNode.getName(), updatedNode.getUrl(), nodeId))
      .onComplete(rowSetAsyncResult -> {
        if (rowSetAsyncResult.failed()) {
          handler.handle(Future.failedFuture(rowSetAsyncResult.cause()));
        }

        RowSet<Row> rows = rowSetAsyncResult.result();
        if (rows.rowCount() == 0) {
          handler.handle(Future.failedFuture("No rows updated."));
        }

        handler.handle(Future.succeededFuture());
      });
  }

  private Node rowToEntity(Row row) {
    int id = row.getInteger("id");
    String name = row.getString("name");
    String url = row.getString("url");

    LocalDateTime createdAt = row.getLocalDateTime("created_at");
    Instant createdAtInstant = createdAt.toInstant(ZoneOffset.UTC);

    return new Node(id, name, url, createdAtInstant);
  }

}
