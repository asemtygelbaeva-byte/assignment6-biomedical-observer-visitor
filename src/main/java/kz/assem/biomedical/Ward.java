package kz.assem.biomedical;

import java.util.List;

public record Ward(String id, List<Element> children) implements GroupNode {
    public Ward { children = List.copyOf(children); }
    public <R> R accept(Visitor<R> visitor) { return visitor.visit(this); }
}

