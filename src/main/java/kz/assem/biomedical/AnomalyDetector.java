package kz.assem.biomedical;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public final class AnomalyDetector extends EventSubject<SignalEvent> implements Observer<SignalEvent> {
    private final Map<String, AtomicReference<Measurements>> states;
    private final double threshold;

    public AnomalyDetector(Map<String, AtomicReference<Measurements>> states, double threshold,
                           Consumer<RuntimeException> errors) {
        super(errors);
        this.states = Map.copyOf(states);
        this.threshold = threshold;
    }

    public void onEvent(SignalEvent event) {
        if (Math.abs(event.value()) > threshold) {
            states.get(event.key()).updateAndGet(old ->
                    new Measurements(old.quality(), old.alarms() + 1, old.latencyMs()));
            publish(new SignalEvent(EventType.ALARM, event.patientId(), event.channel(),
                    event.value(), event.backlog(), event.createdNanos(), 1));
        }
    }
}

