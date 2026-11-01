package kz.assem.biomedical;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class ProducerRunner {
    private final ExecutorService executor;
    private final Duration timeout;
    private volatile boolean terminated;

    public ProducerRunner(ExecutorService executor, Duration timeout) {
        if (timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("Positive timeout required.");
        }
        this.executor = java.util.Objects.requireNonNull(executor);
        this.timeout = timeout;
    }

    public void run(List<Runnable> producers) {
        List<Future<?>> futures = new ArrayList<>();
        try {
            for (Runnable producer : producers) futures.add(executor.submit(producer));
            executor.shutdown();
            for (Future<?> future : futures) future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!executor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                throw new IllegalStateException("Producer shutdown timed out.");
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Producer run interrupted.", interrupted);
        } catch (Exception failure) {
            throw new IllegalStateException("Producer run failed.", failure);
        } finally {
            if (!executor.isTerminated()) {
                executor.shutdownNow();
                try {
                    executor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
            terminated = executor.isTerminated();
        }
    }

    public boolean terminated() { return terminated; }
}

