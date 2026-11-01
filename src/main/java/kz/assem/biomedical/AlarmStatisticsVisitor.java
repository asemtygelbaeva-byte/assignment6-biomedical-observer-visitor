package kz.assem.biomedical;

public final class AlarmStatisticsVisitor extends TreeVisitor<Void> {
    private long alarms;
    private int leaves;

    protected Void group(String kind, GroupNode node) {
        for (Element child : node.children()) child.accept(this);
        return null;
    }

    protected Void leaf(String kind, SignalNode node) {
        alarms += node.data().get().alarms();
        leaves++;
        return null;
    }

    public long alarms() { return alarms; }
    public int leaves() { return leaves; }
}

