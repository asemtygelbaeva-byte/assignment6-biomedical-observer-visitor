package kz.assem.biomedical;

public interface Subject<E extends TypedEvent> {
    void subscribe(EventType type, Observer<E> observer);
    void unsubscribe(EventType type, Observer<E> observer);
    void publish(E event);
}

