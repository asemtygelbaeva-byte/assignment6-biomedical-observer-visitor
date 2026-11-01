package kz.assem.biomedical;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProcessingTest {
    @Test
    void adaptiveFilterUsesSixteenSamplesAtNormalLoad() {
        var filter = new AdaptiveFilter(List.of("P1/ECG"), () -> 100, 5000);
        for (int value = 1; value <= 20; value++) filter.onEvent(Fixtures.sample(value));
        var result = filter.result("P1/ECG");
        assertEquals(16, result.windowSize());
        assertEquals(16, result.storedSamples());
        assertEquals(12.5, result.mean());
    }

    @Test
    void highBacklogShrinksTheWindowToFour() {
        var filter = new AdaptiveFilter(List.of("P1/ECG"), () -> 100, 5000);
        for (int value = 1; value <= 8; value++) filter.onEvent(Fixtures.sample(value));
        filter.onEvent(new SignalEvent(EventType.SAMPLE, "P1", "ECG", 9, 50, 100, 1));
        assertEquals(4, filter.result("P1/ECG").windowSize());
        assertEquals(7.5, filter.result("P1/ECG").mean());
    }

    @Test
    void measuredLatencyAlsoShrinksTheWindow() {
        var filter = new AdaptiveFilter(List.of("P1/ECG"), () -> 10000, 5000);
        filter.onEvent(Fixtures.sample(1));
        assertEquals(4, filter.result("P1/ECG").windowSize());
    }

    @Test
    void filterReturnsToNormalWhenLoadFalls() {
        var filter = new AdaptiveFilter(List.of("P1/ECG"), () -> 100, 5000);
        filter.onEvent(new SignalEvent(EventType.SAMPLE, "P1", "ECG", 2, 100, 100, 1));
        filter.onEvent(Fixtures.sample(3));
        assertEquals(16, filter.result("P1/ECG").windowSize());
        assertEquals(2.5, filter.result("P1/ECG").mean());
    }

    @Test
    void monitorReplacesMeasurementsWithoutOverwritingAlarmCount() {
        var states = Fixtures.states();
        var monitor = new RealTimeMonitor(states, () -> 1_000_100);
        monitor.onEvent(Fixtures.sample(1));
        assertEquals(80, states.get("P1/ECG").get().quality());
        assertEquals(1, states.get("P1/ECG").get().latencyMs());
        assertEquals(0, states.get("P1/ECG").get().alarms());
    }

    @Test
    void anomalyPublishesOnlyAboveTheConfiguredSyntheticThreshold() {
        var states = Fixtures.states();
        var alarms = new ArrayList<SignalEvent>();
        var detector = new AnomalyDetector(states, 1, failure -> fail(failure));
        detector.subscribe(EventType.ALARM, alarms::add);
        detector.onEvent(Fixtures.sample(1));
        detector.onEvent(Fixtures.sample(1.2));
        assertEquals(1, alarms.size());
        assertEquals(1, states.get("P1/ECG").get().alarms());
    }

    @Test
    void latencyTrackerHasHandCalculatedMetrics() {
        var tracker = new LatencyTracker(() -> 2_000_100);
        tracker.onEvent(Fixtures.sample(1));
        tracker.onEvent(Fixtures.sample(2));
        var metrics = tracker.metrics(1_000_000_000);
        assertEquals(2, metrics.samples());
        assertEquals(2, metrics.samplesPerSecond());
        assertEquals(2, metrics.averageMs());
        assertEquals(2, metrics.maximumMs());
    }
}

