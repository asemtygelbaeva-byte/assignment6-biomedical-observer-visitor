package kz.assem.biomedical;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VisitorTest {
    @Test
    void latencySumMatchesOnePlusTwoPlusThreePlusFourPlusTen() {
        assertEquals(20, Fixtures.tree().hospital().accept(new LatencyVisitor()));
    }

    @Test
    void statefulVisitorAccumulatesSevenAlarmsAcrossFiveLeaves() {
        var visitor = new AlarmStatisticsVisitor();
        Fixtures.tree().hospital().accept(visitor);
        assertEquals(7, visitor.alarms());
        assertEquals(5, visitor.leaves());
    }

    @Test
    void filterReturnsActualLeafObjectsBelowEighty() {
        var tree = Fixtures.tree();
        List<Element> result = tree.hospital().accept(new LowQualityVisitor(80));
        assertEquals(List.of("P1/EEG", "P2/ECG"), result.stream().map(Element::id).toList());
        assertSame(tree.first().children().get(2), result.get(0));
    }

    @Test
    void alarmInspectionTraversesOnlyTheRelatedPatient() {
        var tree = Fixtures.tree();
        var result = new ArrayList<SignalEvent>();
        var reports = new EventSubject<SignalEvent>(failure -> fail(failure));
        reports.subscribe(EventType.REPORT, result::add);
        var inspection = new InspectionListener(java.util.Map.of("P1", tree.first(), "P2", tree.second()),
                reports, new LatencyVisitor(), () -> 100);
        inspection.onEvent(new SignalEvent(EventType.ALARM, "P1", "ECG", 2, 0, 100, 1));
        assertEquals(1, result.size());
        assertEquals("P1", result.get(0).patientId());
        assertEquals(10, result.get(0).value());
        assertEquals(20, tree.hospital().accept(new LatencyVisitor()));
    }

    @Test
    void compositeCopiesItsChildrenAndPreventsMutation() {
        var children = new ArrayList<Element>();
        children.add(new EcgChannel("x", Fixtures.data(90, 0, 1)));
        var patient = new Patient("P", children);
        children.clear();
        assertEquals(1, patient.children().size());
        assertThrows(UnsupportedOperationException.class, () -> patient.children().clear());
    }

    @Test
    void jsonHasExactContainerNesting() {
        var hospital = new Hospital("H", List.of(new Ward("W", List.of(new Patient("P",
                List.of(new EcgChannel("E", Fixtures.data(90, 2, 1))))))));
        assertEquals("{\"kind\":\"Hospital\",\"id\":\"H\",\"children\":[{\"kind\":\"Ward\",\"id\":\"W\",\"children\":["
                + "{\"kind\":\"Patient\",\"id\":\"P\",\"children\":[{\"kind\":\"ECG\",\"id\":\"E\","
                + "\"quality\":90.0,\"alarms\":2,\"latencyMs\":1.0}]}]}]}", hospital.accept(new JsonVisitor()));
    }

    @Test
    void jsonEscapesQuotesBackslashesAndControlCharacters() {
        var hospital = new Hospital("H\"\\\n\t\u0001", List.of());
        assertEquals("{\"kind\":\"Hospital\",\"id\":\"H\\\"\\\\\\n\\t\\u0001\",\"children\":[]}",
                hospital.accept(new JsonVisitor()));
    }

    @Test
    void emptyContainerHasZeroLatencyAndNoMatches() {
        var empty = new Hospital("empty", List.of());
        assertEquals(0, empty.accept(new LatencyVisitor()));
        assertTrue(empty.accept(new LowQualityVisitor(80)).isEmpty());
    }
}

