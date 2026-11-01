package kz.assem.biomedical;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

public final class EventLog implements Observer<SignalEvent> {
    private final Map<EventType, LongAdder> counts;
    private final AtomicReference<SignalEvent> lastReport = new AtomicReference<>();

    public EventLog() {
        var initialized = new EnumMap<EventType, LongAdder>(EventType.class);
        for (EventType type : EventType.values()) initialized.put(type, new LongAdder());
        counts = Map.copyOf(initialized);
    }

    public void onEvent(SignalEvent event) {
        counts.get(event.type()).increment();
        if (event.type() == EventType.REPORT) lastReport.set(event);
    }

    public long count(EventType type) { return counts.get(type).sum(); }
    public SignalEvent lastReport() { return lastReport.get(); }
}

