package kz.assem.biomedical;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

public final class Main {
    private record Application(Hospital hospital, StreamSimulation simulation, AdaptiveFilter filter,
                               EventLog log, ConcurrentLinkedQueue<RuntimeException> failures) { }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        if (args.length > 0 && args[0].equals("benchmark")) {
            benchmark(args.length > 1 ? Integer.parseInt(args[1]) : 30000);
            return;
        }
        int threads = args.length > 0 ? Integer.parseInt(args[0]) : 3;
        int samples = args.length > 1 ? Integer.parseInt(args[1]) : 6000;
        Application app = build(threads);
        LatencyTracker.Metrics metrics = app.simulation().run(samples, threads);
        verify(app, samples, metrics);
        System.out.println("Biomedical streams: synthetic ECG / PPG / EEG");
        System.out.printf("Producers=%d samples=%d throughput=%.0f samples/s average=%.4f ms max=%.4f ms%n",
                threads, metrics.samples(), metrics.samplesPerSecond(), metrics.averageMs(), metrics.maximumMs());
        System.out.printf("Alarms=%d related-patient reports=%d aggregates=%d listener errors=%d%n",
                app.log().count(EventType.ALARM), app.log().count(EventType.REPORT),
                app.log().count(EventType.AGGREGATE), app.failures().size());
        System.out.println("Filter window starts at 16; final P1/ECG window=" + app.filter().result("P1/ECG").windowSize());
        System.out.println("Latest patient inspection: " + app.log().lastReport());
        System.out.println("Tree latency sum: " + app.hospital().accept(new LatencyVisitor()));
        var statistics = new AlarmStatisticsVisitor();
        app.hospital().accept(statistics);
        System.out.println("Tree alarm total=" + statistics.alarms() + " leaves=" + statistics.leaves());
        System.out.println("Low quality leaves: " + app.hospital().accept(new LowQualityVisitor(85)).stream().map(Element::id).toList());
        System.out.println(app.hospital().accept(new JsonVisitor()));
    }

    private static void benchmark(int samples) {
        System.out.println("producers,samples,samples_per_second,average_latency_ms,max_latency_ms");
        for (int threads : List.of(1, 2, 4)) {
            Application warmup = build(threads);
            warmup.simulation().run(3000, threads);
            Application app = build(threads);
            LatencyTracker.Metrics metrics = app.simulation().run(samples, threads);
            verify(app, samples, metrics);
            System.out.printf("%d,%d,%.2f,%.6f,%.6f%n", threads, metrics.samples(),
                    metrics.samplesPerSecond(), metrics.averageMs(), metrics.maximumMs());
        }
    }

    private static void verify(Application app, int expected, LatencyTracker.Metrics metrics) {
        if (metrics.samples() != expected || !app.failures().isEmpty()
                || app.log().count(EventType.ALARM) != app.log().count(EventType.REPORT)) {
            throw new IllegalStateException("Incomplete run or listener failure.");
        }
    }

    private static Application build(int threads) {
        LongSupplier clock = System::nanoTime;
        var failures = new ConcurrentLinkedQueue<RuntimeException>();
        var states = new LinkedHashMap<String, AtomicReference<Measurements>>();
        var patients = new LinkedHashMap<String, Patient>();
        for (String id : List.of("P1", "P2")) {
            var children = new ArrayList<Element>();
            for (String channel : List.of("ECG", "PPG", "EEG", "Monitor")) {
                states.put(id + "/" + channel, new AtomicReference<>(new Measurements(90, 0, 1)));
            }
            children.add(new EcgChannel(id + "/ECG", states.get(id + "/ECG")));
            children.add(new PpgChannel(id + "/PPG", states.get(id + "/PPG")));
            children.add(new EegChannel(id + "/EEG", states.get(id + "/EEG")));
            children.add(new MonitorDevice(id + "/Monitor", states.get(id + "/Monitor")));
            patients.put(id, new Patient(id, children));
        }
        var hospital = new Hospital("Teaching hospital", List.of(new Ward("Signals ward", new ArrayList<>(patients.values()))));
        var monitor = new RealTimeMonitor(states, clock);
        var detector = new AnomalyDetector(states, 1.0, failures::add);
        var filter = new AdaptiveFilter(new ArrayList<>(states.keySet()), clock, 5_000_000);
        var aggregator = new PatientAggregator(new ArrayList<>(patients.keySet()), 25, clock, failures::add);
        var tracker = new LatencyTracker(clock);
        var log = new EventLog();
        var reports = new EventSubject<SignalEvent>(failures::add);
        var inspection = new InspectionListener(patients, reports, new LatencyVisitor(), clock);
        detector.subscribe(EventType.ALARM, inspection);
        detector.subscribe(EventType.ALARM, log);
        aggregator.subscribe(EventType.AGGREGATE, log);
        reports.subscribe(EventType.REPORT, log);
        var sources = List.of(
                new SyntheticSource("ECG", new ArrayList<>(patients.keySet()), i -> Math.sin(i * 0.17), clock, failures::add),
                new SyntheticSource("PPG", new ArrayList<>(patients.keySet()), i -> Math.sin(i * 0.11 + 0.3), clock, failures::add),
                new SyntheticSource("EEG", new ArrayList<>(patients.keySet()), i -> Math.sin(i * 0.31 + 0.7), clock, failures::add));
        for (SyntheticSource source : sources) {
            for (Observer<SignalEvent> observer : List.of(monitor, detector, filter, aggregator, tracker)) {
                source.subscribe(EventType.SAMPLE, observer);
            }
        }
        var runner = new ProducerRunner(Executors.newFixedThreadPool(threads), Duration.ofSeconds(30));
        return new Application(hospital, new StreamSimulation(sources, runner, tracker, clock), filter, log, failures);
    }
}

