package kz.assem.biomedical;

import java.util.ArrayList;
import java.util.List;

public final class LowQualityVisitor extends TreeVisitor<List<Element>> {
    private final double threshold;

    public LowQualityVisitor(double threshold) { this.threshold = threshold; }

    protected List<Element> group(String kind, GroupNode node) {
        var matches = new ArrayList<Element>();
        for (Element child : node.children()) matches.addAll(child.accept(this));
        return List.copyOf(matches);
    }

    protected List<Element> leaf(String kind, SignalNode node) {
        return node.data().get().quality() < threshold ? List.of(node) : List.of();
    }
}

