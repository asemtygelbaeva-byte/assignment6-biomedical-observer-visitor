package kz.assem.biomedical;

@FunctionalInterface
public interface Observer<E extends TypedEvent> {
    void onEvent(E event);
}

