package io.vertx.poller.backend.node;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.time.Instant;

@DataObject(generateConverter = true)
public class Node {
  int id;
  String name;
  String url;
  Instant createdAt;

  public Node(int id, String name, String url, Instant createdAt) {
    this.id = id;
    this.name = name;
    this.url = url;
    this.createdAt = createdAt;
  }

  public Node(int id, String name, String url) {
    this.id = id;
    this.name = name;
    this.url = url;
    this.createdAt = Instant.now();
  }

  public Node(JsonObject jsonObject) {
    NodeConverter.fromJson(jsonObject, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    NodeConverter.toJson(this, json);
    return json;
  }

  public String getName() {
    return name;
  }

  public int getId() {
    return id;
  }

  public String getUrl() {
    return url;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @Override
  public String toString() {
    return String.format("Node(id=%d, name='%s', url='%s')", this.id, this.name, this.url);
  }
}
