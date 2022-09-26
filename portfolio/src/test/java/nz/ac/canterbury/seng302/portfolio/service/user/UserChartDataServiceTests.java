package nz.ac.canterbury.seng302.portfolio.service.user;

import nz.ac.canterbury.seng302.portfolio.model.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.model.evidence.PortfolioEvidence;
import nz.ac.canterbury.seng302.portfolio.model.user.User;
import nz.ac.canterbury.seng302.portfolio.service.evidence.EvidenceService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AutoConfigureTestDatabase
@SpringBootTest
public class UserChartDataServiceTests {

    @Autowired
    private UserChartDataService userChartDataService;
    private final User user = new User(UserResponse.newBuilder().setId(1).build());
    private final int testParentProjectId = 1;
    private final String TEST_DESCRIPTION = "According to all known laws of aviation, there is no way a bee should be able to fly.";

    @Test
    void whenUserHasNoEvidence_testGetUserSkillData() {
        Map<String, Integer> result = userChartDataService.getUserSkillData(user.getId(), testParentProjectId, Date.valueOf("2022-01-10"), Date.valueOf("2022-02-10"));
        assertEquals(0, result.size());
    }

    @Test
     void whenUserHasEvidenceWithNoSkills_testGetUserSkillData() {
        Evidence evidence = new Evidence(user.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-01-11"));
        PortfolioEvidence portfolioEvidence = new PortfolioEvidence(evidence, new ArrayList<>());

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(portfolioEvidence)).when(mockedEvidenceService).getEvidenceForPortfolio(user.getId(), testParentProjectId);
        userChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = userChartDataService.getUserSkillData(user.getId(), testParentProjectId, Date.valueOf("2022-01-10"), Date.valueOf("2022-02-10"));
        assertEquals(0, result.size());
    }

    @Test
    void whenUserHasEvidenceWithOneSkill_testGetUserSkillData() {
        Evidence evidence = new Evidence(user.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-01-11"));
        evidence.addSkill("test");
        PortfolioEvidence portfolioEvidence = new PortfolioEvidence(evidence, new ArrayList<>());

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(portfolioEvidence)).when(mockedEvidenceService).getEvidenceForPortfolio(user.getId(), testParentProjectId);
        userChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = userChartDataService.getUserSkillData(user.getId(), testParentProjectId, Date.valueOf("2022-01-10"), Date.valueOf("2022-02-10"));
        assertEquals(1, result.get("TEST"));
    }

    @Test
    void whenUserHasEvidenceWithTwoSkills_testGetUserSkillData() {
        Evidence evidence = new Evidence(user.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-01-11"));
        evidence.addSkill("test");
        evidence.addSkill("junit");
        PortfolioEvidence portfolioEvidence = new PortfolioEvidence(evidence, new ArrayList<>());

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(portfolioEvidence)).when(mockedEvidenceService).getEvidenceForPortfolio(user.getId(), testParentProjectId);
        userChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = userChartDataService.getUserSkillData(user.getId(), testParentProjectId, Date.valueOf("2022-01-10"), Date.valueOf("2022-02-10"));
        assertEquals(1, result.get("TEST"));
        assertEquals(1, result.get("JUNIT"));
    }

    @Test
    void whenUserHasTwoEvidenceWithSameSkill_testGetUserSkillData() {
        Evidence evidence = new Evidence(user.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-01-11"));
        evidence.addSkill("test");
        PortfolioEvidence portfolioEvidence = new PortfolioEvidence(evidence, new ArrayList<>());
        List<PortfolioEvidence> evidenceList = new ArrayList<>();
        evidenceList.add(portfolioEvidence);
        evidenceList.add(portfolioEvidence);
        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(evidenceList).when(mockedEvidenceService).getEvidenceForPortfolio(user.getId(), testParentProjectId);
        userChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = userChartDataService.getUserSkillData(user.getId(), testParentProjectId, Date.valueOf("2022-01-10"), Date.valueOf("2022-02-10"));
        assertEquals(2, result.get("TEST"));
    }

    @Test
     void whenUserHasTwoEvidenceWithDifferentSkills_testGetUserSkillData() {
        Evidence evidence1 = new Evidence(user.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-01-11"));
        Evidence evidence2 = new Evidence(user.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-01-11"));
        evidence1.addSkill("test");
        evidence2.addSkill("jUnit");
        PortfolioEvidence portfolioEvidence1 = new PortfolioEvidence(evidence1, new ArrayList<>());
        PortfolioEvidence portfolioEvidence2 = new PortfolioEvidence(evidence2, new ArrayList<>());
        List<PortfolioEvidence> evidenceList = new ArrayList<>();
        evidenceList.add(portfolioEvidence1);
        evidenceList.add(portfolioEvidence2);

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(evidenceList).when(mockedEvidenceService).getEvidenceForPortfolio(user.getId(), testParentProjectId);
        userChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = userChartDataService.getUserSkillData(user.getId(), testParentProjectId, Date.valueOf("2022-01-10"), Date.valueOf("2022-02-10"));
        assertEquals(1, result.get("TEST"));
        assertEquals(1, result.get("JUNIT"));
    }

    @Test
    void whenUserHasEvidenceWithSkillInWrongProject_testGetUserSkillData() {
        Evidence evidence = new Evidence(user.getId(), 2, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-01-11"));
        evidence.addSkill("test");
        PortfolioEvidence portfolioEvidence = new PortfolioEvidence(evidence, new ArrayList<>());
        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(portfolioEvidence)).when(mockedEvidenceService).getEvidenceForPortfolio(user.getId(), testParentProjectId);
        userChartDataService.setEvidenceService(mockedEvidenceService);
        Map<String, Integer> result = userChartDataService.getUserSkillData(user.getId(), testParentProjectId, Date.valueOf("2022-01-10"), Date.valueOf("2022-02-10"));

        assertEquals(0, result.size());
    }

    @ParameterizedTest
    @CsvSource({"2022-01-09,0", "2022-01-10,1", "2022-01-11,1", "2022-02-09,1", "2022-02-10,1", "2022-02-11,0",  })
    void whenOneUserWithEvidenceWithSkillTestDateBoundaries_testGetUserSkillData(String evidenceDate, int expectedResult) {
        Evidence evidence = new Evidence(user.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf(evidenceDate));
        evidence.addSkill("test");
        PortfolioEvidence portfolioEvidence = new PortfolioEvidence(evidence, new ArrayList<>());
        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(portfolioEvidence)).when(mockedEvidenceService).getEvidenceForPortfolio(user.getId(), testParentProjectId);
        userChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = userChartDataService.getUserSkillData(user.getId(), testParentProjectId, Date.valueOf("2022-01-10"), Date.valueOf("2022-02-10"));
        assertEquals(expectedResult, result.size());
    }
}
