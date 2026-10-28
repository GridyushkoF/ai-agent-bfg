package com.aichat.aichatdemo2.platform.util;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class UtcTime {

    private UtcTime() {
    }

    public static OffsetDateTime toUtc(OffsetDateTime value) {
        if (value == null) {
            return null;
        }
        return value.withOffsetSameInstant(ZoneOffset.UTC);
    }
}
