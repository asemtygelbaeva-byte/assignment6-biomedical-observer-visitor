package kz.assem.biomedical;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrencyTest {
    @Test
    @Timeout(10)
    void fourThreadsPublishOneThousandEventsEachWithoutLoss() {
        var errors = new ConcurrentLinkedQueue<RuntimeException>();
        var subject = new EventSubject<SignalEvent>(errors::add);
        var counts = List.of(new AtomicLong(), new AtomicLong(), new AtomicLong(), new AtomicLong(), new AtomicLong());
        for (AtomicLong count : counts) subject.subscribe(EventType.SAMPLE, event -> count.incrementAndGet());
        var aggregator = new PatientAggregator(List.of("P1"), 25, () -> 100, errors::add);
        var batches = new AtomicLong();
        aggregator.subscribe(EventType.AGGREGATE, event -> batches.incrementAndGet());
        subject.subscribe(EventType.SAMPLE, aggregator);
        var tasks = new ArrayList<Runnable>();
        for (int index = 0; index < 4; index++) {
            tasks.add(() -> {
                for (int sample = 0; sample < 1000; sample++) subject.publish(Fixtures.sample(1));
            });
        }
        var runner = new ProducerRunner(Executors.newFixedThreadPool(4), Duration.ofSeconds(5));
        runner.run(tasks);
        for (AtomicLong count : counts) assertEquals(4000, count.get());
        assertEquals(4000, aggregator.samples("P1"));
        assertEquals(160, batches.get());
        assertTrue(errors.isEmpty());
        assertTrue(runner.terminated());
    }

    @Test
    @Timeout(10)
    void handlerCanLeaveSafelyDuringConcurrentPublishing() {
        var subject = new EventSubject<SignalEvent>(failure -> fail(failure));
        var stable = new AtomicLong();
        var leaving = new AtomicLong();
        subject.subscribe(EventType.SAMPLE, event -> stable.incrementAndGet());
        Observer<SignalEvent> listener = new Observer<>() {
            public void onEvent(SignalEvent event) {
                leaving.incrementAndGet();
                subject.unsubscribe(EventType.SAMPLE, this);
            }
        };
        subject.subscribe(EventType.SAMPLE, listener);
        var task = (Runnable) () -> {
            for (int sample = 0; sample < 1000; sample++) subject.publish(Fixtures.sample(1));
        };
        var runner = new ProducerRunner(Executors.newFixedThreadPool(4), Duration.ofSeconds(5));
        runner.run(List.of(task, task, task, task));
        assertEquals(4000, stable.get());
        assertTrue(leaving.get() >= 1 && leaving.get() <= 4);
        assertTrue(runner.terminated());
    }

    @Test
    void completedRunDrainsEveryProducerBeforeReturning() {
        var count = new AtomicLong();
        var runner = new ProducerRunner(Executors.newFixedThreadPool(3), Duration.ofSeconds(5));
        runner.run(List.of(() -> count.addAndGet(100), () -> count.addAndGet(200), () -> count.addAndGet(300)));
        assertEquals(600, count.get());
        assertTrue(runner.terminated());
    }
}

