package kz.assem.biomedical;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

public final class PatientAggregator extends EventSubject<SignalEvent> implements Observer<SignalEvent> {
    private record Total(long count, double sum) { }

    private final Map<String, AtomicReference<Total>> totals;
    private final int batchSize;
    private final LongSupplier clock;

    public PatientAggregator(List<String> patients, int batchSize, LongSupplier clock,
                             Consumer<RuntimeException> errors) {
        super(errors);
        if (batchSize < 1) throw new IllegalArgumentException("Batch size must be positive.");
        var initialized = new HashMap<String, AtomicReference<Total>>();
        for (String patient : patients) initialized.put(patient, new AtomicReference<>(new Total(0, 0)));
        totals = Map.copyOf(initialized);
        this.batchSize = batchSize;
        this.clock = clock;
    }

    public void onEvent(SignalEvent event) {
        Total total = totals.get(event.patientId()).updateAndGet(old ->
                new Total(old.count() + 1, old.sum() + event.value()));
        if (total.count() % batchSize == 0) {
            publish(new SignalEvent(EventType.AGGREGATE, event.patientId(), "ALL",
                    total.sum() / total.count(), 0, clock.getAsLong(), total.count()));
        }
    }

    public long samples(String patientId) { return totals.get(patientId).get().count(); }
}

