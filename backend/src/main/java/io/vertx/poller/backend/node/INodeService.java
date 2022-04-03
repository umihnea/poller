package io.vertx.poller.backend.node;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.List;

@VertxGen
@ProxyGen
public interface INodeService {
  void all(Handler<AsyncResult<List<Node>>> handler);

  void create(JsonObject data, Handler<AsyncResult<JsonObject>> handler);

  void delete(Integer nodeId, Handler<AsyncResult<Void>> handler);

  void update(Integer nodeId, JsonObject data, Handler<AsyncResult<Void>> handler);

  static INodeService createProxy(Vertx vertx, String address) {
    return new INodeServiceVertxEBProxy(vertx, address);
  }
}
