package kz.assem.biomedical;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public class EventSubject<E extends TypedEvent> implements Subject<E> {
    private final Map<EventType, CopyOnWriteArraySet<Observer<E>>> listeners;
    private final Consumer<RuntimeException> errors;

    public EventSubject(Consumer<RuntimeException> errors) {
        this.errors = Objects.requireNonNull(errors);
        var registry = new EnumMap<EventType, CopyOnWriteArraySet<Observer<E>>>(EventType.class);
        for (EventType type : EventType.values()) registry.put(type, new CopyOnWriteArraySet<>());
        listeners = Map.copyOf(registry);
    }

    public void subscribe(EventType type, Observer<E> observer) {
        listeners.get(type).add(Objects.requireNonNull(observer));
    }

    public void unsubscribe(EventType type, Observer<E> observer) {
        listeners.get(type).remove(observer);
    }

    public void publish(E event) {
        for (Observer<E> observer : listeners.get(event.type())) {
            try {
                observer.onEvent(event);
            } catch (RuntimeException failure) {
                errors.accept(failure);
            }
        }
    }
}

