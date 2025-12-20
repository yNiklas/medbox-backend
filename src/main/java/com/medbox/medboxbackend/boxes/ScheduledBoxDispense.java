package com.medbox.medboxbackend.boxes;

import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

public record ScheduledBoxDispense(
        String mosMac,
        String boxMac,
        int compartmentNumber,
        ScheduledFuture<?> scheduledFuture
) {
    boolean isOfStack(String mosMac) {
        return Objects.equals(this.mosMac, mosMac);
    }
}
