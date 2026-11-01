package kz.assem.biomedical;

public final class LatencyVisitor extends TreeVisitor<Double> {
    protected Double group(String kind, GroupNode node) {
        return node.children().stream().mapToDouble(child -> child.accept(this)).sum();
    }

    protected Double leaf(String kind, SignalNode node) {
        return node.data().get().latencyMs();
    }
}

