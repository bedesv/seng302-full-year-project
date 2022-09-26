package nz.ac.canterbury.seng302.portfolio.model.evidence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AutoConfigureTestDatabase
@SpringBootTest
class EvidenceTests {
    private Evidence testEvidence;

    @BeforeEach
    void resetTestEvidence() {
        testEvidence = new Evidence();
    }

    @Test
    void whenNoCommits_testRemoveCommit() {
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> testEvidence.removeCommit(0));
        assertEquals("Evidence has less than 1 commits. Commit not deleted.", exception.getMessage());
    }

    @Test
    void whenNoCommits_testRemoveCommitWithIndexTooLow() {
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> testEvidence.removeCommit(-1));
        assertEquals("Commit index is invalid because it is less than one. Commit not deleted.", exception.getMessage());
    }

    @Test
    void whenOneCommit_testRemoveCommit() {
        testEvidence.addCommit(new Commit());
        assertEquals(1, testEvidence.getNumberCommits());
        testEvidence.removeCommit(0);
        assertEquals(0, testEvidence.getNumberCommits());
    }

    @Test
    void whenOneCommit_testRemoveCommitWithIndexTooHigh() {
        testEvidence.addCommit(new Commit());
        assertEquals(1, testEvidence.getNumberCommits());
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> testEvidence.removeCommit(1));
        assertEquals("Evidence has less than 2 commits. Commit not deleted.", exception.getMessage());
        assertEquals(1, testEvidence.getNumberCommits());
    }

    @Test
    void whenOneCommit_testRemoveCommitWithIndexTooLow() {
        testEvidence.addCommit(new Commit());
        assertEquals(1, testEvidence.getNumberCommits());
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> testEvidence.removeCommit(-1));
        assertEquals("Commit index is invalid because it is less than one. Commit not deleted.", exception.getMessage());
        assertEquals(1, testEvidence.getNumberCommits());
    }
}