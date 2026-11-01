package kz.assem.biomedical;

import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.LongSupplier;

public final class LatencyTracker implements Observer<SignalEvent> {
    public record Metrics(long samples, double samplesPerSecond, double averageMs, double maximumMs) { }

    private final LongAdder samples = new LongAdder();
    private final LongAdder latencyNanos = new LongAdder();
    private final LongAccumulator maximum = new LongAccumulator(Long::max, 0);
    private final LongSupplier clock;

    public LatencyTracker(LongSupplier clock) { this.clock = clock; }

    public void onEvent(SignalEvent event) {
        long latency = Math.max(0, clock.getAsLong() - event.createdNanos());
        latencyNanos.add(latency);
        maximum.accumulate(latency);
        samples.increment();
    }

    public Metrics metrics(long elapsedNanos) {
        long count = samples.sum();
        return new Metrics(count, count * 1_000_000_000.0 / Math.max(1, elapsedNanos),
                count == 0 ? 0 : latencyNanos.sum() / (count * 1_000_000.0),
                maximum.get() / 1_000_000.0);
    }
}

