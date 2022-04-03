package io.vertx.poller.backend.history;

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

public class RecordRepository {
  private static final Logger LOG = LoggerFactory.getLogger(RecordRepository.class);
  MySQLPool pooledClient;

  public RecordRepository(MySQLPool pooledClient) {
    this.pooledClient = pooledClient;
  }

  public void create(Record record, Handler<AsyncResult<Void>> handler) {
    Tuple attributes = Tuple.of(
      record.nodeId,
      record.statusCode,
      record.state,
      TimeConvertor.databaseTimestampFromInstant(record.createdAt),
      record.delay
    );

    this.pooledClient
      .preparedQuery("insert into record (node_id, status_code, state, created_at, delay) values (?, ?, ?, ?, ?);")
      .execute(attributes)
      .onComplete(insertionResult -> {
        if (insertionResult.failed()) {
          handler.handle(Future.failedFuture(
              new Throwable("Could not insert record.")
            )
          );

          return;
        }

        handler.handle(Future.succeededFuture());
      });
  }

  public void queryInterval(Instant start, Instant end, Handler<AsyncResult<List<Record>>> handler) {
    Tuple attributes = Tuple.of(
      TimeConvertor.databaseTimestampFromInstant(start),
      TimeConvertor.databaseTimestampFromInstant(end)
    );

    this.pooledClient
      .preparedQuery(
        "select * from record where to_seconds(?) <= to_seconds(created_at) AND to_seconds(created_at) <= to_seconds(?);"
      )
      .execute(attributes)
      .onComplete(queryResult -> {
        if (queryResult.failed()) {
          handler.handle(Future.failedFuture(queryResult.cause()));
        }

        RowSet<Row> rowSet = queryResult.result();
        List<Record> records = new ArrayList<>();
        for (Row row : rowSet) {
          records.add(this.rowToEntity(row));
        }

        handler.handle(Future.succeededFuture(records));
      });
  }

  private Record rowToEntity(Row row) {
    Integer id = row.getInteger("id");
    Integer nodeId = row.getInteger("node_id");
    Integer statusCode = row.getInteger("status_code");
    Integer state = row.getInteger("state");

    LocalDateTime createdAt = row.getLocalDateTime("created_at");
    Instant createdAtInstant = createdAt.toInstant(ZoneOffset.UTC);

    long delay = row.getInteger("delay");

    return new Record(id, nodeId, statusCode, state, createdAtInstant, delay);
  }
}
