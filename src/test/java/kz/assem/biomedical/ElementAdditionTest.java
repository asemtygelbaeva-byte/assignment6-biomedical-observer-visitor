package kz.assem.biomedical;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ElementAdditionTest {
    @Test
    void newElementParticipatesInEveryExistingOperation() {
        var oxygen = new OxygenSensor("P/Oxygen", Fixtures.data(40, 1, 5));
        var tree = new Hospital("H", List.of(new Ward("W", List.of(new Patient("P", List.of(oxygen))))));
        assertEquals(5, tree.accept(new LatencyVisitor()));
        var statistics = new AlarmStatisticsVisitor();
        tree.accept(statistics);
        assertEquals(1, statistics.alarms());
        assertEquals(1, statistics.leaves());
        assertEquals(List.of(oxygen), tree.accept(new LowQualityVisitor(80)));
        assertEquals(40, tree.accept(new QualityVisitor()).average());
        assertTrue(tree.accept(new JsonVisitor()).contains("\"kind\":\"OxygenSensor\""));
    }
}
