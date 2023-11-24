package livecast.agent.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateTimeUtil {
    private final ZoneId utcTimeZone = ZoneId.of("UTC");

    public DateTimeUtil() {
    }

    public LocalDateTime toUTCLocalDateTime(LocalDateTime dateTime, ZoneId timeZone) {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, timeZone);
        return this.toZoneLocalDateTime(zonedDateTime, this.utcTimeZone);
    }

    public LocalDateTime toZoneLocalDateTime(ZonedDateTime zonedDateTime, ZoneId timeZone) {
        return zonedDateTime.withZoneSameInstant(timeZone).toLocalDateTime();
    }
}
