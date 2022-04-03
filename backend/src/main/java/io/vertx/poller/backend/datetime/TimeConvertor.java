package io.vertx.poller.backend.datetime;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TimeConvertor {
  public static String databaseTimestampFromInstant(Instant instant) {
    return DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC)).format(instant);
  }
}
