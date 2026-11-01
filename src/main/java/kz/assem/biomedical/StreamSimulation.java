package kz.assem.biomedical;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;

public final class StreamSimulation {
    private final List<SyntheticSource> sources;
    private final ProducerRunner runner;
    private final LatencyTracker tracker;
    private final LongSupplier clock;

    public StreamSimulation(List<SyntheticSource> sources, ProducerRunner runner,
                            LatencyTracker tracker, LongSupplier clock) {
        this.sources = List.copyOf(sources);
        this.runner = runner;
        this.tracker = tracker;
        this.clock = clock;
    }

    public LatencyTracker.Metrics run(int samples, int producers) {
        if (samples < 1 || producers < 1) throw new IllegalArgumentException("Positive run size required.");
        var tasks = new ArrayList<Runnable>();
        for (int producer = 0; producer < producers; producer++) {
            int start = producer;
            tasks.add(() -> {
                for (int sequence = start; sequence < samples; sequence += producers) {
                    if (Thread.currentThread().isInterrupted()) return;
                    sources.get(sequence % sources.size()).emit(sequence);
                }
            });
        }
        long start = clock.getAsLong();
        runner.run(tasks);
        return tracker.metrics(clock.getAsLong() - start);
    }
}

