package kz.assem.biomedical;

import java.util.Objects;

public record SignalEvent(EventType type, String patientId, String channel, double value,
                          int backlog, long createdNanos, long count) implements TypedEvent {
    public SignalEvent {
        Objects.requireNonNull(type);
        Objects.requireNonNull(patientId);
        Objects.requireNonNull(channel);
        if (!Double.isFinite(value) || backlog < 0 || count < 1) {
            throw new IllegalArgumentException("Invalid signal event.");
        }
    }

    public String key() { return patientId + "/" + channel; }
}

