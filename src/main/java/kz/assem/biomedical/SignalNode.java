package kz.assem.biomedical;

import java.util.concurrent.atomic.AtomicReference;

public interface SignalNode extends Element {
    AtomicReference<Measurements> data();
}

