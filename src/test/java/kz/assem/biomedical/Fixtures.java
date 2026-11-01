package kz.assem.biomedical;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

final class Fixtures {
    record Tree(Hospital hospital, Patient first, Patient second) { }

    static AtomicReference<Measurements> data(double quality, long alarms, double latency) {
        return new AtomicReference<>(new Measurements(quality, alarms, latency));
    }

    static Tree tree() {
        var first = new Patient("P1", List.of(
                new EcgChannel("P1/ECG", data(90, 2, 1)),
                new PpgChannel("P1/PPG", data(80, 1, 2)),
                new EegChannel("P1/EEG", data(70, 0, 3)),
                new MonitorDevice("P1/Monitor", data(100, 0, 4))));
        var second = new Patient("P2", List.of(new EcgChannel("P2/ECG", data(60, 4, 10))));
        return new Tree(new Hospital("H", List.of(new Ward("W", List.of(first, second)))), first, second);
    }

    static SignalEvent sample(double value) {
        return new SignalEvent(EventType.SAMPLE, "P1", "ECG", value, 0, 100, 1);
    }

    static Map<String, AtomicReference<Measurements>> states() {
        return Map.of("P1/ECG", data(90, 0, 1));
    }
}

