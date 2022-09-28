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
        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(new ArrayList<>()).when(mockedEvidenceService).getEvidenceForPortfolio(user.getId(), testParentProjectId);
        userChartDataService.setEvidenceService(mockedEvidenceService);

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
        Evidence evidence1 = new Evidence(user.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-01-11"));
        Evidence evidence2 = new Evidence(user.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-01-11"));
        evidence1.addSkill("test");
        evidence2.addSkill("junit");
        PortfolioEvidence portfolioEvidence1 = new PortfolioEvidence(evidence1, new ArrayList<>());
        PortfolioEvidence portfolioEvidence2 = new PortfolioEvidence(evidence2, new ArrayList<>());
        List<PortfolioEvidence> portfolioEvidenceList = new ArrayList<>();
        portfolioEvidenceList.add(portfolioEvidence1);
        portfolioEvidenceList.add(portfolioEvidence2);

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(portfolioEvidenceList).when(mockedEvidenceService).getEvidenceForPortfolio(user.getId(), testParentProjectId);
        userChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = userChartDataService.getUserSkillData(user.getId(), testParentProjectId, Date.valueOf("2022-01-10"), Date.valueOf("2022-02-10"));
        assertEquals(1, result.get("TEST"));
        assertEquals(1, result.get("JUNIT"));
    }

    @Test
    void whenUserHasTwoEvidenceWithSameSkill_testGetUserSkillData() {
        Evidence evidence1 = new Evidence(user.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-01-11"));
        Evidence evidence2 = new Evidence(user.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-01-11"));
        evidence1.addSkill("test");
        evidence2.addSkill("test");

        PortfolioEvidence portfolioEvidence1 = new PortfolioEvidence(evidence1, new ArrayList<>());
        PortfolioEvidence portfolioEvidence2 = new PortfolioEvidence(evidence2, new ArrayList<>());
        List<PortfolioEvidence> evidenceList = new ArrayList<>();
        evidenceList.add(portfolioEvidence1);
        evidenceList.add(portfolioEvidence2);

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
        Mockito.doReturn(List.of(portfolioEvidence)).when(mockedEvidenceService).getEvidenceForPortfolio(user.getId(), 2);
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

    //////////////////////////OverTime Tests///////////////////////////

    @Test
    void givenOneUser_withNoEvidence_getEvidenceOverTime(){
        Date startDate = Date.valueOf("2022-05-01");
        Date endDate = Date.valueOf("2022-06-01");

        Map<String, Integer> result = userChartDataService.getUserEvidenceData(user, testParentProjectId, startDate, endDate);

        assertTrue(result.values()
                .stream()
                .allMatch(x -> x == 0));
    }

    @Test
    void givenOneUser_withOneEvidence_getEvidenceOverTime(){
        Date startDate = Date.valueOf("2022-05-01");
        Date endDate = Date.valueOf("2022-06-01");

        Evidence testEvidence1A = new Evidence(user.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-04-30"));
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1)).when(mockedEvidenceService).getEvidenceForPortfolio(user.getId(), testParentProjectId);

        Map<String, Integer> result = userChartDataService.getUserEvidenceData(user, testParentProjectId, startDate, endDate);
        assertEquals(1, result.get("2022-05-01"));
    }

    @Test
    void givenOneUser_withTwoEvidences_getEvidenceOverTime(){
        Date startDate = Date.valueOf("2022-05-01");
        Date endDate = Date.valueOf("2022-06-01");

        Evidence testEvidence1A = new Evidence(user.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-05-02"));
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());
        Evidence testEvidence2A = new Evidence(user.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-05-15"));
        PortfolioEvidence testEvidence2 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1), List.of(testEvidence2)).when(mockedEvidenceService).getEvidenceForPortfolio(user.getId(), testParentProjectId);

        Map<String, Integer> result = userChartDataService.getUserEvidenceData(user, testParentProjectId, startDate, endDate);
        assertEquals(1, result.get("2022-05-02"));
        assertEquals(1, result.get("2022-05-15"));
    }

    @Test
    void givenOneUser_withOneEvidenceEachOfBeforeDuringAfterProjectDates_getEvidenceOverTime() {
        Date startDate = Date.valueOf("2022-05-01");
        Date endDate = Date.valueOf("2022-06-01");

        Evidence testEvidence1A = new Evidence(user.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-04-15"));
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());
        Evidence testEvidence2A = new Evidence(user.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-05-15"));
        PortfolioEvidence testEvidence2 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());
        Evidence testEvidence3A = new Evidence(user.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-06-15"));
        PortfolioEvidence testEvidence3 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1), List.of(testEvidence2), List.of(testEvidence3)).when(mockedEvidenceService).getEvidenceForPortfolio(user.getId(), testParentProjectId);

        Map<String, Integer> result = userChartDataService.getUserEvidenceData(user, testParentProjectId, startDate, endDate);
        assertEquals(1, result.get("2022-05-15"));
    }

}
