package kz.assem.biomedical;

import java.util.List;

public record Hospital(String id, List<Element> children) implements GroupNode {
    public Hospital { children = List.copyOf(children); }
    public <R> R accept(Visitor<R> visitor) { return visitor.visit(this); }
}

