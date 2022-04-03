package io.vertx.poller.backend.history;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.List;

@VertxGen
@ProxyGen
public interface IHistoryService {
  void logFromPollData(JsonObject pollData, Handler<AsyncResult<Void>> handler);

  void recordsInInterval(JsonObject queryData, Handler<AsyncResult<List<Record>>> handler);

  static IHistoryService createProxy(Vertx vertx, String address) {
    return new IHistoryServiceVertxEBProxy(vertx, address);
  }
}
