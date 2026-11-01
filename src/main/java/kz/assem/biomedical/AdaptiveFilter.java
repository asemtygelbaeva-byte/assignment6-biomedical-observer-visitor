package kz.assem.biomedical;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongSupplier;

public final class AdaptiveFilter implements Observer<SignalEvent> {
    public record Result(int windowSize, int storedSamples, double mean) { }

    private static final class Window {
        private final ArrayDeque<Double> values = new ArrayDeque<>();
        private int size = 16;
        private double sum;

        synchronized void add(double value, int targetSize) {
            size = targetSize;
            values.addLast(value);
            sum += value;
            while (values.size() > size) sum -= values.removeFirst();
        }

        synchronized Result result() {
            return new Result(size, values.size(), values.isEmpty() ? 0 : sum / values.size());
        }
    }

    private final Map<String, Window> windows;
    private final LongSupplier clock;
    private final long latencyLimitNanos;

    public AdaptiveFilter(List<String> keys, LongSupplier clock, long latencyLimitNanos) {
        var initialized = new HashMap<String, Window>();
        for (String key : keys) initialized.put(key, new Window());
        windows = Map.copyOf(initialized);
        this.clock = clock;
        this.latencyLimitNanos = latencyLimitNanos;
    }

    public void onEvent(SignalEvent event) {
        boolean overloaded = event.backlog() >= 50
                || clock.getAsLong() - event.createdNanos() > latencyLimitNanos;
        windows.get(event.key()).add(event.value(), overloaded ? 4 : 16);
    }

    public Result result(String key) { return windows.get(key).result(); }
}

