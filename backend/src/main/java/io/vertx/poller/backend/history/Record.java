package io.vertx.poller.backend.history;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.time.Instant;

@DataObject(generateConverter = true)
public class Record {
  int id;
  int nodeId;
  Integer statusCode;
  int state;
  Instant createdAt;
  long delay;  // in milliseconds

  public Record(int id, Integer nodeId, Integer statusCode, int state, Instant createdAt, long delay) {
    this.id = id;
    this.nodeId = nodeId;
    this.statusCode = statusCode;
    this.state = state;
    this.createdAt = createdAt;
    this.delay = delay;
  }

  public Record(JsonObject jsonObject) {
    RecordConverter.fromJson(jsonObject, this);
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    RecordConverter.toJson(this, jsonObject);
    return jsonObject;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public int getId() {
    return id;
  }

  public int getState() {
    return state;
  }

  public Integer getStatusCode() {
    return statusCode;
  }

  public int getNodeId() {
    return nodeId;
  }

  public long getDelay() {
    return delay;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public void setDelay(long delay) {
    this.delay = delay;
  }

  public void setNodeId(int nodeId) {
    this.nodeId = nodeId;
  }

  public void setState(int state) {
    this.state = state;
  }

  public void setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
  }

  @Override
  public String toString() {
    return String.format("Record(id=%d, nodeId=%d, code=%d, state=%d, delay=%d)", this.id, this.nodeId, this.statusCode, this.state, this.delay);
  }
}
