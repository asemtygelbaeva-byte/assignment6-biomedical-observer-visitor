package kz.assem.biomedical;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

public final class RealTimeMonitor implements Observer<SignalEvent> {
    private final Map<String, AtomicReference<Measurements>> states;
    private final LongSupplier clock;

    public RealTimeMonitor(Map<String, AtomicReference<Measurements>> states, LongSupplier clock) {
        this.states = Map.copyOf(states);
        this.clock = clock;
    }

    public void onEvent(SignalEvent event) {
        double quality = Math.max(0, 100 - Math.abs(event.value()) * 20);
        double latency = Math.max(0, clock.getAsLong() - event.createdNanos()) / 1_000_000.0;
        states.get(event.key()).updateAndGet(old -> new Measurements(quality, old.alarms(), latency));
    }
}

