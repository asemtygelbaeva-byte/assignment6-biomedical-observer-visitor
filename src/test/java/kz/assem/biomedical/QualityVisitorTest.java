package kz.assem.biomedical;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class QualityVisitorTest {
    @Test
    void newOperationUsesTheExistingTreeWithoutElementChanges() {
        var score = Fixtures.tree().hospital().accept(new QualityVisitor());
        assertEquals(400, score.total());
        assertEquals(5, score.leaves());
        assertEquals(80, score.average());
    }

    @Test
    void weightedAverageDoesNotTreatUnequalPatientSubtreesAsEqual() {
        var tree = Fixtures.tree();
        assertEquals(85, tree.first().accept(new QualityVisitor()).average());
        assertEquals(60, tree.second().accept(new QualityVisitor()).average());
        assertEquals(80, tree.hospital().accept(new QualityVisitor()).average());
        assertEquals(0, new Hospital("empty", List.of()).accept(new QualityVisitor()).average());
    }
}

