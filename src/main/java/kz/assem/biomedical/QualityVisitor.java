package kz.assem.biomedical;

public final class QualityVisitor extends TreeVisitor<QualityVisitor.Score> {
    public record Score(double total, int leaves) {
        public double average() { return leaves == 0 ? 0 : total / leaves; }
    }

    protected Score group(String kind, GroupNode node) {
        double total = 0;
        int leaves = 0;
        for (Element child : node.children()) {
            Score score = child.accept(this);
            total += score.total();
            leaves += score.leaves();
        }
        return new Score(total, leaves);
    }

    protected Score leaf(String kind, SignalNode node) {
        return new Score(node.data().get().quality(), 1);
    }
}

