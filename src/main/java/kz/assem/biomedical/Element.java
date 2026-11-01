package kz.assem.biomedical;

public interface Element {
    String id();
    <R> R accept(Visitor<R> visitor);
}

