package kz.assem.biomedical;

public abstract class TreeVisitor<R> implements Visitor<R> {
    protected abstract R group(String kind, GroupNode node);
    protected abstract R leaf(String kind, SignalNode node);

    public R visit(Hospital node) { return group("Hospital", node); }
    public R visit(Ward node) { return group("Ward", node); }
    public R visit(Patient node) { return group("Patient", node); }
    public R visit(EcgChannel node) { return leaf("ECG", node); }
    public R visit(PpgChannel node) { return leaf("PPG", node); }
    public R visit(EegChannel node) { return leaf("EEG", node); }
    public R visit(MonitorDevice node) { return leaf("Monitor", node); }
    public R visit(OxygenSensor node) { return leaf("OxygenSensor", node); }
}

