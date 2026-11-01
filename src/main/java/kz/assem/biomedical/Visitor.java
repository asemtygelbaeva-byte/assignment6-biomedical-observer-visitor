package kz.assem.biomedical;

public interface Visitor<R> {
    R visit(Hospital node);
    R visit(Ward node);
    R visit(Patient node);
    R visit(EcgChannel node);
    R visit(PpgChannel node);
    R visit(EegChannel node);
    R visit(MonitorDevice node);
    R visit(OxygenSensor node);
}

