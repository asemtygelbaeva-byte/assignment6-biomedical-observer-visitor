package kz.assem.biomedical;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ObserverTest {
    @Test
    void deliversOnlyTheSubscribedEventType() {
        var errors = new ArrayList<RuntimeException>();
        var subject = new EventSubject<SignalEvent>(errors::add);
        var count = new AtomicInteger();
        subject.subscribe(EventType.SAMPLE, event -> count.incrementAndGet());
        subject.publish(new SignalEvent(EventType.ALARM, "P1", "ECG", 2, 0, 100, 1));
        subject.publish(Fixtures.sample(1));
        assertEquals(1, count.get());
    }

    @Test
    void duplicateSubscriptionDoesNotDuplicateDelivery() {
        var subject = new EventSubject<SignalEvent>(failure -> fail(failure));
        var count = new AtomicInteger();
        Observer<SignalEvent> listener = event -> count.incrementAndGet();
        subject.subscribe(EventType.SAMPLE, listener);
        subject.subscribe(EventType.SAMPLE, listener);
        subject.publish(Fixtures.sample(1));
        assertEquals(1, count.get());
    }

    @Test
    void listenerCanUnsubscribeInsideItsOwnHandler() {
        var subject = new EventSubject<SignalEvent>(failure -> fail(failure));
        var count = new AtomicInteger();
        Observer<SignalEvent> listener = new Observer<>() {
            public void onEvent(SignalEvent event) {
                count.incrementAndGet();
                subject.unsubscribe(EventType.SAMPLE, this);
            }
        };
        subject.subscribe(EventType.SAMPLE, listener);
        subject.publish(Fixtures.sample(1));
        subject.publish(Fixtures.sample(2));
        assertEquals(1, count.get());
    }

    @Test
    void listenerAddedInsideHandlerStartsAtTheNextPublication() {
        var subject = new EventSubject<SignalEvent>(failure -> fail(failure));
        var late = new AtomicInteger();
        Observer<SignalEvent> added = event -> late.incrementAndGet();
        subject.subscribe(EventType.SAMPLE, event -> subject.subscribe(EventType.SAMPLE, added));
        subject.publish(Fixtures.sample(1));
        assertEquals(0, late.get());
        subject.publish(Fixtures.sample(2));
        assertEquals(1, late.get());
    }

    @Test
    void listenerFailureDoesNotPreventOtherListenersReceivingTheEvent() {
        var errors = new ArrayList<RuntimeException>();
        var subject = new EventSubject<SignalEvent>(errors::add);
        var count = new AtomicInteger();
        subject.subscribe(EventType.SAMPLE, event -> { throw new IllegalStateException("test"); });
        subject.subscribe(EventType.SAMPLE, event -> count.incrementAndGet());
        subject.publish(Fixtures.sample(1));
        assertEquals(1, errors.size());
        assertEquals(1, count.get());
    }

    @Test
    void twoIndependentSourcesReachTheSameListener() {
        var first = new EventSubject<SignalEvent>(failure -> fail(failure));
        var second = new EventSubject<SignalEvent>(failure -> fail(failure));
        var count = new AtomicInteger();
        Observer<SignalEvent> listener = event -> count.incrementAndGet();
        first.subscribe(EventType.SAMPLE, listener);
        second.subscribe(EventType.SAMPLE, listener);
        first.publish(Fixtures.sample(1));
        second.publish(Fixtures.sample(2));
        assertEquals(2, count.get());
    }

    @Test
    void aggregateIsRepublishedThroughASecondObserverLink() {
        var aggregates = new ArrayList<SignalEvent>();
        var aggregator = new PatientAggregator(List.of("P1"), 2, () -> 100, failure -> fail(failure));
        aggregator.subscribe(EventType.AGGREGATE, aggregates::add);
        aggregator.onEvent(Fixtures.sample(2));
        aggregator.onEvent(Fixtures.sample(4));
        assertEquals(1, aggregates.size());
        assertEquals(3, aggregates.get(0).value());
        assertEquals(2, aggregates.get(0).count());
    }
}

