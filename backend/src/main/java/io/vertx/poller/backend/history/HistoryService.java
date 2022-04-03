package io.vertx.poller.backend.history;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class HistoryService implements IHistoryService {
  private static final Logger LOG = LoggerFactory.getLogger(HistoryService.class);
  RecordRepository repository;

  public HistoryService(RecordRepository repository) {
    this.repository = repository;
  }

  @Override
  public void logFromPollData(JsonObject pollData, Handler<AsyncResult<Void>> handler) {
    Instant finishedAt = pollData.getInstant("finishedAt");
    Instant emittedAt = pollData.getInstant("emittedAt");
    long delay = this.delayInMilliseconds(emittedAt, finishedAt);
    int state = this.stateFromStatus(pollData.getInteger("status")).ordinal();
    Record record = new Record(
      -1,
      pollData.getInteger("nodeId"),
      pollData.getInteger("status"),
      state,
      finishedAt,
      delay
    );

    this.repository.create(record, asyncResult -> {
      if (asyncResult.failed()) {
        handler.handle(Future.failedFuture(asyncResult.cause()));
        return;
      }

      handler.handle(Future.succeededFuture());
    });
  }

  private APIState stateFromStatus(Integer status) {
    if (status == null) {
      return APIState.DOWN;
    }

    if (200 <= status && status <= 299) {
      return APIState.UP;
    }

    return APIState.DOWN;
  }

  private long delayInMilliseconds(Instant emittedAt, Instant finishedAt) {
    return Duration.between(emittedAt, finishedAt).toMillis();
  }

  @Override
  public void recordsInInterval(JsonObject queryData, Handler<AsyncResult<List<Record>>> handler) {
    Instant start = queryData.getInstant("start");
    Instant end = queryData.getInstant("end");
    this.repository.queryInterval(start, end, result -> {
      if (result.failed()) {
        LOG.error(
          String.format("Query for interval (%s, %s) failed in repository.", start, end)
        );
        handler.handle(Future.failedFuture(result.cause()));
        return;
      }

      List<Record> nodes = new ArrayList<>(result.result());
      handler.handle(Future.succeededFuture(nodes));
    });
  }
}
