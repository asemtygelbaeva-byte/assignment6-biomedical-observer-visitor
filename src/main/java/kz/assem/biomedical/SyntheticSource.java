package kz.assem.biomedical;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;
import java.util.function.LongSupplier;

public final class SyntheticSource extends EventSubject<SignalEvent> {
    private final String channel;
    private final List<String> patients;
    private final DoubleUnaryOperator wave;
    private final LongSupplier clock;

    public SyntheticSource(String channel, List<String> patients, DoubleUnaryOperator wave,
                           LongSupplier clock, Consumer<RuntimeException> errors) {
        super(errors);
        this.channel = channel;
        this.patients = List.copyOf(patients);
        this.wave = wave;
        this.clock = clock;
    }

    public void emit(int sequence) {
        double value = wave.applyAsDouble(sequence) + Math.sin(sequence * 0.73) * 0.05;
        int simulatedBacklog = sequence % 200 < 100 ? 4 : 100;
        publish(new SignalEvent(EventType.SAMPLE, patients.get(sequence % patients.size()), channel,
                value, simulatedBacklog, clock.getAsLong(), 1));
    }
}

