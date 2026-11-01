package kz.assem.biomedical;

import java.util.Map;
import java.util.function.LongSupplier;

public final class InspectionListener implements Observer<SignalEvent> {
    private final Map<String, Patient> patients;
    private final Subject<SignalEvent> reports;
    private final Visitor<Double> visitor;
    private final LongSupplier clock;

    public InspectionListener(Map<String, Patient> patients, Subject<SignalEvent> reports,
                              Visitor<Double> visitor, LongSupplier clock) {
        this.patients = Map.copyOf(patients);
        this.reports = reports;
        this.visitor = visitor;
        this.clock = clock;
    }

    public void onEvent(SignalEvent alarm) {
        Patient related = patients.get(alarm.patientId());
        double latency = related.accept(visitor);
        reports.publish(new SignalEvent(EventType.REPORT, related.id(), "inspection-latency",
                latency, 0, clock.getAsLong(), 1));
    }
}

