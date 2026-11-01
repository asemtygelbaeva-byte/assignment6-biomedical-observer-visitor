package kz.assem.biomedical;

import java.util.concurrent.atomic.AtomicReference;

public record PpgChannel(String id, AtomicReference<Measurements> data) implements SignalNode {
    public <R> R accept(Visitor<R> visitor) { return visitor.visit(this); }
}

