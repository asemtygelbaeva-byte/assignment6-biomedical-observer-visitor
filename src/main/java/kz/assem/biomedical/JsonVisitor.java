package kz.assem.biomedical;

import java.util.stream.Collectors;

public final class JsonVisitor extends TreeVisitor<String> {
    protected String group(String kind, GroupNode node) {
        String children = node.children().stream().map(child -> child.accept(this))
                .collect(Collectors.joining(","));
        return "{\"kind\":" + quote(kind) + ",\"id\":" + quote(node.id()) + ",\"children\":[" + children + "]}";
    }

    protected String leaf(String kind, SignalNode node) {
        Measurements data = node.data().get();
        return "{\"kind\":" + quote(kind) + ",\"id\":" + quote(node.id())
                + ",\"quality\":" + data.quality() + ",\"alarms\":" + data.alarms()
                + ",\"latencyMs\":" + data.latencyMs() + "}";
    }

    static String quote(String value) {
        var output = new StringBuilder("\"");
        for (char character : value.toCharArray()) {
            switch (character) {
                case '"' -> output.append("\\\"");
                case '\\' -> output.append("\\\\");
                case '\n' -> output.append("\\n");
                case '\r' -> output.append("\\r");
                case '\t' -> output.append("\\t");
                case '\b' -> output.append("\\b");
                case '\f' -> output.append("\\f");
                default -> {
                    if (character < 32) output.append(String.format("\\u%04x", (int) character));
                    else output.append(character);
                }
            }
        }
        return output.append('"').toString();
    }
}

