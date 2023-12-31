package nz.ac.canterbury.seng302.portfolio.service.group;

import nz.ac.canterbury.seng302.portfolio.model.evidence.Categories;
import nz.ac.canterbury.seng302.portfolio.model.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.model.evidence.PortfolioEvidence;
import nz.ac.canterbury.seng302.portfolio.model.group.Group;
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

import java.util.*;
import java.sql.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;


@AutoConfigureTestDatabase
@SpringBootTest
class GroupChartDataServiceTests {

    @Autowired
    private GroupChartDataService groupChartDataService;
    private final User testUser1 = new User(UserResponse.newBuilder().setId(1).build());
    private final User testUser2 = new User(UserResponse.newBuilder().setId(2).build());
    private static final int testParentProjectId = 1;
    private static final int testGroupId = 1;
    private static final Date startDate = Date.valueOf("2022-01-01");
    private static final Date dateInBound = Date.valueOf("2023-01-01");
    private static final Date endDate = Date.valueOf("2024-01-01");

    private static final String TEST_DESCRIPTION = "According to all known laws of aviation, there is no way a bee should be able to fly.";

    //Skills//
    @Test
    void whenNoUsersInGroup_testGetGroupSkillData() {
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, new ArrayList<>());
        Map<String, Integer> result = groupChartDataService.getGroupSkillData(testGroup, Date.valueOf("2022-01-10"), Date.valueOf("2022-02-10"));
        assertEquals(0, result.size());
    }

    @Test
    void whenOneUserWithNoEvidenceInGroup_testGetGroupSkillData() {
        List<User> testUserList = new ArrayList<>();
        testUserList.add(testUser1);
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(new ArrayList<>()).when(mockedEvidenceService).getEvidenceForPortfolio(testUser1.getId(), testParentProjectId);
        groupChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = groupChartDataService.getGroupSkillData(testGroup, Date.valueOf("2022-01-10"), Date.valueOf("2022-02-10"));
        assertEquals(0, result.size());
    }

    @Test
    void whenOneUserWithEvidenceWithNoSkillsInGroup_testGetGroupSkillData() {
        List<User> testUserList = new ArrayList<>();
        testUserList.add(testUser1);
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);
        Evidence testEvidence1A = new Evidence(testUser1.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-01-11"));
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1)).when(mockedEvidenceService).getEvidenceForPortfolio(testUser1.getId(), testParentProjectId);
        groupChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = groupChartDataService.getGroupSkillData(testGroup, Date.valueOf("2022-01-10"), Date.valueOf("2022-02-10"));
        assertEquals(0, result.size());
    }

    @ParameterizedTest
    @CsvSource({"2022-01-09,0", "2022-01-10,1", "2022-01-11,1", "2022-02-09,1", "2022-02-10,1", "2022-02-11,0",  })
    void whenOneUserWithEvidenceWithSkillsTestDateBoundaries_testGetGroupSkillData(String evidenceDate, int expectedResult) {
        List<User> testUserList = new ArrayList<>();
        testUserList.add(testUser1);
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);
        Evidence testEvidence1A = new Evidence(testUser1.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf(evidenceDate));
        testEvidence1A.addSkill("test");
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());
        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1)).when(mockedEvidenceService).getEvidenceForPortfolio(testUser1.getId(), testParentProjectId);
        groupChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = groupChartDataService.getGroupSkillData(testGroup, Date.valueOf("2022-01-10"), Date.valueOf("2022-02-10"));
        assertEquals(expectedResult, result.size());
    }


    @Test
    void whenOneUserWithEvidenceWithMultipleSkills_testGetGroupSkillData() {
        List<User> testUserList = new ArrayList<>();
        testUserList.add(testUser1);
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);
        Evidence testEvidence1A = new Evidence(testUser1.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-02-05"));
        testEvidence1A.addSkill("test");
        testEvidence1A.addSkill("jUnit");
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());
        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1)).when(mockedEvidenceService).getEvidenceForPortfolio(testUser1.getId(), testParentProjectId);
        groupChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = groupChartDataService.getGroupSkillData(testGroup, Date.valueOf("2022-01-10"), Date.valueOf("2022-02-10"));
        assertEquals(2, result.size());
    }

    @Test
    void whenNoUsersInGroup_testGetGroupEvidenceDataCompareMembers() {
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, new ArrayList<>());
        Map<String, Integer> result = groupChartDataService.getGroupEvidenceDataCompareMembers(testGroup, Date.valueOf("2022-01-10"), Date.valueOf("2022-02-10"));
        assertEquals(0, result.size());
    }

    @Test
    void whenOneUserWithNoEvidenceInGroup_testGetGroupEvidenceDataCompareMembers() {
        List<User> testUserList = new ArrayList<>();
        testUser1.setFirstName("Tester");
        testUser1.setLastName("One");
        testUserList.add(testUser1);
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(new ArrayList<>()).when(mockedEvidenceService).getEvidenceForPortfolio(testUser1.getId(), testParentProjectId);
        groupChartDataService.setEvidenceService(mockedEvidenceService);
       Map<String, Integer> result = groupChartDataService.getGroupEvidenceDataCompareMembers(testGroup, Date.valueOf("2022-01-10"), Date.valueOf("2022-02-10"));
       assertEquals(0, result.get(testUser1.getId() + " " + testUser1.getFirstAndLast()));
    }

    @Test
    void whenOneUserWithEvidenceInGroup_testGetGroupEvidenceDataCompareMembers() {
        List<User> testUserList = new ArrayList<>();
        testUser1.setFirstName("Tester");
        testUser1.setLastName("One");
        testUserList.add(testUser1);
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);
        Evidence testEvidence1A = new Evidence();
        testEvidence1A.setDate(Date.valueOf("2022-01-15"));
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1)).when(mockedEvidenceService).getEvidenceForPortfolio(testUser1.getId(), testParentProjectId);
        groupChartDataService.setEvidenceService(mockedEvidenceService);
        Map<String, Integer> result = groupChartDataService.getGroupEvidenceDataCompareMembers(testGroup, Date.valueOf("2022-01-10"), Date.valueOf("2022-02-10"));
        assertEquals(1, result.get(testUser1.getId() + " " + testUser1.getFirstAndLast()));
    }

    @Test
    void whenTwoUserWithEvidenceInGroup_testGetGroupEvidenceDataCompareMembers() {
        List<User> testUserList = new ArrayList<>();
        testUser1.setFirstName("Tester");
        testUser1.setLastName("One");
        testUser2.setFirstName("Tester");
        testUser2.setLastName("Two");
        testUserList.add(testUser1);
        testUserList.add(testUser2);
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);
        Evidence testEvidence1A = new Evidence();
        testEvidence1A.setDate(Date.valueOf("2022-01-15"));
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1), List.of(testEvidence1)).when(mockedEvidenceService).getEvidenceForPortfolio(any(int.class), any(int.class));
        groupChartDataService.setEvidenceService(mockedEvidenceService);
        Map<String, Integer> result = groupChartDataService.getGroupEvidenceDataCompareMembers(testGroup, Date.valueOf("2022-01-10"), Date.valueOf("2022-02-10"));

        assertEquals(1, result.get(testUser1.getId() + " " + testUser1.getFirstAndLast()));
        assertEquals(1, result.get(testUser2.getId() + " " + testUser2.getFirstAndLast()));
    }

    @Test
    void whenTwoUserInGroupOneWithOneWithout_testGetGroupEvidenceDataCompareMembers() {
        List<User> testUserList = new ArrayList<>();
        testUser1.setFirstName("Tester");
        testUser1.setLastName("One");
        testUser2.setFirstName("Tester");
        testUser2.setLastName("Two");
        testUserList.add(testUser1);
        testUserList.add(testUser2);

        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);
        Evidence testEvidence1A = new Evidence();
        testEvidence1A.setDate(Date.valueOf("2022-01-30"));
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1), new ArrayList<>()).when(mockedEvidenceService).getEvidenceForPortfolio(any(int.class), any(int.class));
        groupChartDataService.setEvidenceService(mockedEvidenceService);
        Map<String, Integer> result = groupChartDataService.getGroupEvidenceDataCompareMembers(testGroup, Date.valueOf("2022-01-10"), Date.valueOf("2022-02-10"));
        assertEquals(1, result.get(testUser1.getId() + " " + testUser1.getFirstAndLast()));
        assertEquals(0, result.get(testUser2.getId() + " " + testUser2.getFirstAndLast()));
    }


    //////////////////////////OverTime Tests///////////////////////////

    //Day//
    @Test
    void whenNoUsersInGroup_testGetGroupEvidenceDataOverTimeDay() {
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, new ArrayList<>());
        Date startDate = Date.valueOf("2022-05-01");
        Date endDate = Date.valueOf("2022-06-01");
        Map<String, Integer> result = groupChartDataService.getGroupEvidenceDataOverTime(testGroup, startDate, endDate, "day");
        assertEquals(32, result.size());
        assertEquals(0, result.get("2022-05-01"));
    }

    @Test
    void whenUserInGroupWithEvidenceOutOfBounds_testGetGroupEvidenceOverTimeDay() {
        List<User> testUserList = new ArrayList<>();
        Date startDate = Date.valueOf("2022-05-01");
        Date endDate = Date.valueOf("2022-06-01");
        testUserList.add(testUser1);
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);
        Evidence testEvidence1A = new Evidence(testUser1.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-04-30"));
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1)).when(mockedEvidenceService).getEvidenceForPortfolio(testUser1.getId(), testParentProjectId);

        groupChartDataService.setEvidenceService(mockedEvidenceService);
        Map<String, Integer> result = groupChartDataService.getGroupEvidenceDataOverTime(testGroup, startDate, endDate, "day");
        assertEquals(32, result.size());
        assertFalse(result.containsKey("2022-04-30"));
    }

    @Test
    void whenUserInGroupWithEvidenceOnLowerBound_testGetGroupEvidenceOverTimeDay() {
        List<User> testUserList = new ArrayList<>();
        Date startDate = Date.valueOf("2022-05-01");
        Date endDate = Date.valueOf("2022-06-01");
        testUserList.add(testUser1);
        Evidence testEvidence1A = new Evidence(testUser1.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-05-01"));
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());

        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1)).when(mockedEvidenceService).getEvidenceForPortfolio(testUser1.getId(), testParentProjectId);
        groupChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = groupChartDataService.getGroupEvidenceDataOverTime(testGroup, startDate, endDate, "day");
        assertEquals(1, result.get("2022-05-01"));
    }

    @Test
    void whenUserInGroupWithEvidenceOnUpperBound_testGetGroupEvidenceOverTimeDay() {
        List<User> testUserList = new ArrayList<>();
        Date startDate = Date.valueOf("2022-05-01");
        Date endDate = Date.valueOf("2022-06-01");
        testUserList.add(testUser1);
        Evidence testEvidence1A = new Evidence(testUser1.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-06-01"));
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());

        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1)).when(mockedEvidenceService).getEvidenceForPortfolio(testUser1.getId(), testParentProjectId);
        groupChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = groupChartDataService.getGroupEvidenceDataOverTime(testGroup, startDate, endDate, "day");
        assertEquals(1, result.get("2022-06-01"));
    }

    @Test
    void whenUserInGroupWithEvidenceInsideBoundary_testGetGroupEvidenceOverTimeDay() {
        List<User> testUserList = new ArrayList<>();
        Date startDate = Date.valueOf("2022-05-01");
        Date endDate = Date.valueOf("2022-06-01");
        testUserList.add(testUser1);
        Evidence testEvidence1A = new Evidence(testUser1.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-05-10"));
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());

        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1)).when(mockedEvidenceService).getEvidenceForPortfolio(testUser1.getId(), testParentProjectId);
        groupChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = groupChartDataService.getGroupEvidenceDataOverTime(testGroup, startDate, endDate, "day");
        assertEquals(1, result.get("2022-05-10"));
    }

    @Test
    void whenTwoUsersInGroupWithEvidenceInsideBoundary_testGetGroupEvidenceOverTimeDay() {
        List<User> testUserList = new ArrayList<>();
        Date startDate = Date.valueOf("2022-05-01");
        Date endDate = Date.valueOf("2022-06-01");
        testUserList.add(testUser1);
        testUserList.add(testUser2);
        Evidence testEvidence1A = new Evidence(testUser1.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-05-10"));
        Evidence testEvidence2A = new Evidence(testUser2.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-05-10"));
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());
        PortfolioEvidence testEvidence2 = new PortfolioEvidence(testEvidence2A, new ArrayList<>());
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1), List.of(testEvidence2)).when(mockedEvidenceService).getEvidenceForPortfolio(any(int.class), any(int.class));
        groupChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = groupChartDataService.getGroupEvidenceDataOverTime(testGroup, startDate, endDate, "day");
        assertEquals(2, result.get("2022-05-10"));
    }

    //Week//
    @Test
    void whenUserInGroupWithEvidenceOutOfBounds_testGetGroupEvidenceOverTimeWeek() {
        List<User> testUserList = new ArrayList<>();
        Date startDate = Date.valueOf("2022-05-01");
        Date endDate = Date.valueOf("2022-06-01");
        testUserList.add(testUser1);
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);
        Evidence testEvidence1A = new Evidence(testUser1.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-04-30"));
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());
        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1)).when(mockedEvidenceService).getEvidenceForPortfolio(testUser1.getId(), testParentProjectId);

        groupChartDataService.setEvidenceService(mockedEvidenceService);
        Map<String, Integer> result = groupChartDataService.getGroupEvidenceDataOverTime(testGroup, startDate, endDate, "week");
        assertEquals(6, result.size());
        assertEquals(0, result.get("2022-05-01"));
    }

    @Test
    void whenStartDateAndEndDateBothOnSunday_testGetGroupEvidenceOverTimeWeek() {
        Date startDate = Date.valueOf("2022-05-01");
        Date endDate = Date.valueOf("2022-06-05");
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, new ArrayList<>());
        Map<String, Integer> result = groupChartDataService.getGroupEvidenceDataOverTime(testGroup, startDate, endDate, "week");
        assertEquals(6, result.size());
    }

    @Test
    void whenUserInGroupWithEvidenceOnLowerBound_testGetGroupEvidenceOverTimeWeek() {
        List<User> testUserList = new ArrayList<>();
        Date startDate = Date.valueOf("2022-05-01");
        Date endDate = Date.valueOf("2022-06-01");
        testUserList.add(testUser1);
        Evidence testEvidence1A = new Evidence(testUser1.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-05-01"));
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1)).when(mockedEvidenceService).getEvidenceForPortfolio(testUser1.getId(), testParentProjectId);
        groupChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = groupChartDataService.getGroupEvidenceDataOverTime(testGroup, startDate, endDate, "week");
        assertEquals(1, result.get("2022-05-01")); //Gets the Sunday value
    }

    @Test
    void whenUserInGroupWithEvidenceOnUpperBound_testGetGroupEvidenceOverTimeWeek() {
        List<User> testUserList = new ArrayList<>();
        Date startDate = Date.valueOf("2022-05-01");
        Date endDate = Date.valueOf("2022-06-01");
        testUserList.add(testUser1);
        Evidence testEvidence1A = new Evidence(testUser1.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-06-01"));
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1)).when(mockedEvidenceService).getEvidenceForPortfolio(testUser1.getId(), testParentProjectId);
        groupChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = groupChartDataService.getGroupEvidenceDataOverTime(testGroup, startDate, endDate, "week");
        assertEquals(1, result.get("2022-06-05")); //Gets the Sunday value
    }

    @Test
    void whenUserInGroupWithEvidenceOutsideUpperBound_testGetGroupEvidenceOverTimeWeek() {
        List<User> testUserList = new ArrayList<>();
        Date startDate = Date.valueOf("2022-05-01");
        Date endDate = Date.valueOf("2022-06-01");
        testUserList.add(testUser1);
        Evidence testEvidence1A = new Evidence(testUser1.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-06-02"));
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1)).when(mockedEvidenceService).getEvidenceForPortfolio(testUser1.getId(), testParentProjectId);
        groupChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = groupChartDataService.getGroupEvidenceDataOverTime(testGroup, startDate, endDate, "week");
        assertEquals(0, result.get("2022-06-05")); //Gets the Sunday value
    }

    @Test
    void whenUserInGroupWithEvidenceInsideBoundary_testGetGroupEvidenceOverTimeWeek() {
        List<User> testUserList = new ArrayList<>();
        Date startDate = Date.valueOf("2022-05-01");
        Date endDate = Date.valueOf("2022-06-01");
        testUserList.add(testUser1);
        Evidence testEvidence1A = new Evidence(testUser1.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-05-10"));
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1)).when(mockedEvidenceService).getEvidenceForPortfolio(testUser1.getId(), testParentProjectId);
        groupChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = groupChartDataService.getGroupEvidenceDataOverTime(testGroup, startDate, endDate, "week");
        assertEquals(1, result.get("2022-05-15")); //Gets the Sunday value
    }

    //////////////////////////Category Tests///////////////////////////
    @Test
    void whenOneUserWithMultipleEvidenceWithMultipleSkills_testGetGroupSkillData() {
        List<User> testUserList = new ArrayList<>();
        testUserList.add(testUser1);
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);
        Evidence testEvidence1A = new Evidence(testUser1.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-02-05"));
        Evidence testEvidence2A = new Evidence(testUser1.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-02-05"));
        testEvidence1A.addSkill("test");
        testEvidence1A.addSkill("jUnit");
        testEvidence2A.addSkill("test");
        testEvidence2A.addSkill("Mocking");
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());
        PortfolioEvidence testEvidence2 = new PortfolioEvidence(testEvidence2A, new ArrayList<>());
        List<PortfolioEvidence> testEvidenceList = new ArrayList<>();
        testEvidenceList.add(testEvidence1);
        testEvidenceList.add(testEvidence2);

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(testEvidenceList).when(mockedEvidenceService).getEvidenceForPortfolio(testUser1.getId(), testParentProjectId);
        groupChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = groupChartDataService.getGroupSkillData(testGroup, Date.valueOf("2022-01-10"), Date.valueOf("2022-02-10"));
        assertEquals(3, result.size());
        assertEquals(2, result.get("TEST"));
        assertEquals(1, result.get("JUNIT"));
        assertEquals(1, result.get("MOCKING"));
    }

    @Test
    void whenTwoUserWithEvidenceWithSameSkill_testGetGroupSkillData() {
        List<User> testUserList = new ArrayList<>();
        testUserList.add(testUser1);
        testUserList.add(testUser2);
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);
        Evidence testEvidence1A = new Evidence(testUser1.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-02-05"));
        Evidence testEvidence2A = new Evidence(testUser2.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-02-05"));
        testEvidence1A.addSkill("test");
        testEvidence2A.addSkill("test");
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());
        PortfolioEvidence testEvidence2 = new PortfolioEvidence(testEvidence2A, new ArrayList<>());

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1), List.of(testEvidence2)).when(mockedEvidenceService).getEvidenceForPortfolio(any(int.class), any(int.class));
        groupChartDataService.setEvidenceService(mockedEvidenceService);
        Map<String, Integer> result = groupChartDataService.getGroupSkillData(testGroup, Date.valueOf("2022-01-10"), Date.valueOf("2022-02-10"));
        assertEquals(1, result.size());
        assertEquals(2, result.get("TEST"));
    }

    @Test
    void whenTwoUserWithEvidenceWithSameSkillDifferentCapitalization_testGetGroupSkillData() {
        List<User> testUserList = new ArrayList<>();
        testUserList.add(testUser1);
        testUserList.add(testUser2);
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);
        Evidence testEvidence1A = new Evidence(testUser1.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-02-05"));
        Evidence testEvidence2A = new Evidence(testUser2.getId(), testParentProjectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-02-05"));
        testEvidence1A.addSkill("test");
        testEvidence2A.addSkill("Test");
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());
        PortfolioEvidence testEvidence2 = new PortfolioEvidence(testEvidence2A, new ArrayList<>());

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1), List.of(testEvidence2)).when(mockedEvidenceService).getEvidenceForPortfolio(any(int.class), any(int.class));
        groupChartDataService.setEvidenceService(mockedEvidenceService);
        Map<String, Integer> result = groupChartDataService.getGroupSkillData(testGroup, Date.valueOf("2022-01-10"), Date.valueOf("2022-02-10"));
        assertEquals(1, result.size());
        assertEquals(2, result.get("TEST"));
    }

    //Category//
    @Test
    void whenNoUsersInGroup_testGetGroupCategoryData() {
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, new ArrayList<>());
        Map<String, Integer> result = groupChartDataService.getGroupCategoryInfo(testGroup, startDate, endDate);
        assertEquals(0, result.get("Service"));
        assertEquals(0, result.get("Quantitative"));
        assertEquals(0, result.get("Qualitative"));
    }

    @Test
    void whenOneUserWithNoEvidenceInGroup_testGetGroupCategoryData() {
        List<User> testUserList = new ArrayList<>();
        testUserList.add(testUser1);
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(new ArrayList<>()).when(mockedEvidenceService).getEvidenceForPortfolio(testUser1.getId(), testParentProjectId);
        groupChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = groupChartDataService.getGroupCategoryInfo(testGroup, startDate, endDate);
        assertEquals(0, result.get("Service"));
        assertEquals(0, result.get("Quantitative"));
        assertEquals(0, result.get("Qualitative"));
    }

    @Test
    void whenOneUserWithEvidenceWithNoCategoriesInGroup_testGetGroupCategoryData() {
        List<User> testUserList = new ArrayList<>();
        testUserList.add(testUser1);
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);

        Evidence testEvidence1A = new Evidence();
        Evidence testEvidence2A = new Evidence();
        testEvidence1A.setDate(dateInBound);
        testEvidence2A.setDate(dateInBound);
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());
        PortfolioEvidence testEvidence2 = new PortfolioEvidence(testEvidence2A, new ArrayList<>());
        List<PortfolioEvidence> testEvidenceList = new ArrayList<>();
        testEvidenceList.add(testEvidence1);
        testEvidenceList.add(testEvidence2);

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(testEvidenceList).when(mockedEvidenceService).getEvidenceForPortfolio(testUser1.getId(), testParentProjectId);
        groupChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = groupChartDataService.getGroupCategoryInfo(testGroup, startDate, endDate);
        assertEquals(0, result.get("Service"));
        assertEquals(0, result.get("Quantitative"));
        assertEquals(0, result.get("Qualitative"));
    }

    @Test
    void whenOneUserWithEvidenceWithSomeCategoriesInGroup_testGetGroupCategoryData() {
        List<User> testUserList = new ArrayList<>();
        testUserList.add(testUser1);
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);

        Set<Categories> testCategoriesSet = new HashSet<>(Arrays.asList(Categories.QUANTITATIVE, Categories.QUALITATIVE));
        Evidence testEvidence1A = new Evidence();
        testEvidence1A.setDate(dateInBound);
        testEvidence1A.setCategories(testCategoriesSet);
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());
        List<PortfolioEvidence> testEvidenceList = new ArrayList<>();
        testEvidenceList.add(testEvidence1);

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(testEvidenceList).when(mockedEvidenceService).getEvidenceForPortfolio(testUser1.getId(), testParentProjectId);
        groupChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = groupChartDataService.getGroupCategoryInfo(testGroup, startDate, endDate);
        assertEquals(0, result.get("Service"));
        assertEquals(1, result.get("Quantitative"));
        assertEquals(1, result.get("Qualitative"));
    }

    @Test
    void whenOneUserWithEvidenceWithAllCategoriesInGroup_testGetGroupCategoryData() {
        List<User> testUserList = new ArrayList<>();
        testUserList.add(testUser1);
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);

        Set<Categories> testCategoriesSet = new HashSet<>(Arrays.asList(Categories.SERVICE, Categories.QUANTITATIVE, Categories.QUALITATIVE));
        Evidence testEvidence1A = new Evidence();
        Evidence testEvidence2A = new Evidence();
        testEvidence1A.setDate(dateInBound);
        testEvidence2A.setDate(dateInBound);
        testEvidence1A.setCategories(testCategoriesSet);
        testEvidence2A.setCategories(testCategoriesSet);
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());
        PortfolioEvidence testEvidence2 = new PortfolioEvidence(testEvidence2A, new ArrayList<>());
        List<PortfolioEvidence> testEvidenceList = new ArrayList<>();
        testEvidenceList.add(testEvidence1);
        testEvidenceList.add(testEvidence2);

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(testEvidenceList).when(mockedEvidenceService).getEvidenceForPortfolio(testUser1.getId(), testParentProjectId);
        groupChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = groupChartDataService.getGroupCategoryInfo(testGroup, startDate, endDate);
        assertEquals(2, result.get("Service"));
        assertEquals(2, result.get("Quantitative"));
        assertEquals(2, result.get("Qualitative"));
    }

    @Test
    void whenTwoUsersWithNoEvidenceInGroup_testGetGroupCategoryData() {
        List<User> testUserList = new ArrayList<>(Arrays.asList(testUser1, testUser2));
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(new ArrayList<>(), new ArrayList<>()).when(mockedEvidenceService).getEvidenceForPortfolio(any(int.class), any(int.class));
        groupChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = groupChartDataService.getGroupCategoryInfo(testGroup, startDate, endDate);
        assertEquals(0, result.get("Service"));
        assertEquals(0, result.get("Quantitative"));
        assertEquals(0, result.get("Qualitative"));
    }

    @Test
    void whenTwoUsersWithEvidenceWithNoCategoriesInGroup_testGetGroupCategoryData() {
        List<User> testUserList = new ArrayList<>(Arrays.asList(testUser1, testUser2));
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);

        Evidence testEvidence1A = new Evidence();
        Evidence testEvidence2A = new Evidence();
        testEvidence1A.setDate(dateInBound);
        testEvidence2A.setDate(dateInBound);
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());
        PortfolioEvidence testEvidence2 = new PortfolioEvidence(testEvidence2A, new ArrayList<>());

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1), List.of(testEvidence2)).when(mockedEvidenceService).getEvidenceForPortfolio(any(int.class), any(int.class));
        groupChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = groupChartDataService.getGroupCategoryInfo(testGroup, startDate, endDate);
        assertEquals(0, result.get("Service"));
        assertEquals(0, result.get("Quantitative"));
        assertEquals(0, result.get("Qualitative"));
    }

    @Test
    void whenTwoUsersWithEvidenceWithSomeCategoriesInGroup_testGetGroupCategoryData() {
        List<User> testUserList = new ArrayList<>(Arrays.asList(testUser1, testUser2));
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);

        Set<Categories> testCategoriesSet1 = new HashSet<>(Arrays.asList(Categories.QUANTITATIVE, Categories.QUALITATIVE));
        Set<Categories> testCategoriesSet2 = new HashSet<>(Arrays.asList(Categories.QUANTITATIVE, Categories.SERVICE));
        Evidence testEvidence1A = new Evidence();
        Evidence testEvidence2A = new Evidence();
        testEvidence1A.setDate(dateInBound);
        testEvidence2A.setDate(dateInBound);
        testEvidence1A.setCategories(testCategoriesSet1);
        testEvidence2A.setCategories(testCategoriesSet2);
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());
        PortfolioEvidence testEvidence2 = new PortfolioEvidence(testEvidence2A, new ArrayList<>());



        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1), List.of(testEvidence2)).when(mockedEvidenceService).getEvidenceForPortfolio(any(int.class), any(int.class));
        groupChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = groupChartDataService.getGroupCategoryInfo(testGroup, startDate, endDate);
        assertEquals(1, result.get("Service"));
        assertEquals(2, result.get("Quantitative"));
        assertEquals(1, result.get("Qualitative"));
    }

    @Test
    void whenTwoUsersWithEvidenceWithAllCategoriesInGroup_testGetGroupCategoryData() {
        List<User> testUserList = new ArrayList<>(Arrays.asList(testUser1, testUser2));
        Group testGroup = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList);

        Set<Categories> testCategoriesSet = new HashSet<>(Arrays.asList(Categories.QUANTITATIVE, Categories.QUALITATIVE, Categories.SERVICE));
        Evidence testEvidence1A = new Evidence();
        Evidence testEvidence2A = new Evidence();
        testEvidence1A.setDate(dateInBound);
        testEvidence2A.setDate(dateInBound);
        testEvidence1A.setCategories(testCategoriesSet);
        testEvidence2A.setCategories(testCategoriesSet);
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());
        PortfolioEvidence testEvidence2 = new PortfolioEvidence(testEvidence2A, new ArrayList<>());



        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1), List.of(testEvidence2)).when(mockedEvidenceService).getEvidenceForPortfolio(any(int.class), any(int.class));
        groupChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = groupChartDataService.getGroupCategoryInfo(testGroup, startDate, endDate);
        assertEquals(2, result.get("Service"));
        assertEquals(2, result.get("Quantitative"));
        assertEquals(2, result.get("Qualitative"));
    }

    @Test
    void whenTwoUsersWithEvidenceWithAllCategoriesInDifferentGroups_testGetGroupCategoryDataForEachGroup() {
        List<User> testUserList1 = new ArrayList<>(List.of(testUser1));
        List<User> testUserList2 = new ArrayList<>(List.of(testUser1));
        Group testGroup1 = new Group(testGroupId, "Short Name", "Long Name", testParentProjectId, testUserList1);
        Group testGroup2 = new Group(testGroupId + 1, "Short Name 2", "Long Name 2", testParentProjectId, testUserList2);

        Set<Categories> testCategoriesSet = new HashSet<>(Arrays.asList(Categories.QUANTITATIVE, Categories.QUALITATIVE, Categories.SERVICE));
        Evidence testEvidence1A = new Evidence();
        Evidence testEvidence2A = new Evidence();
        testEvidence1A.setDate(dateInBound);
        testEvidence2A.setDate(dateInBound);
        testEvidence1A.setCategories(testCategoriesSet);
        testEvidence2A.setCategories(testCategoriesSet);
        PortfolioEvidence testEvidence1 = new PortfolioEvidence(testEvidence1A, new ArrayList<>());
        PortfolioEvidence testEvidence2 = new PortfolioEvidence(testEvidence2A, new ArrayList<>());

        EvidenceService mockedEvidenceService = Mockito.mock(EvidenceService.class);
        Mockito.doReturn(List.of(testEvidence1), List.of(testEvidence2)).when(mockedEvidenceService).getEvidenceForPortfolio(any(int.class), any(int.class));
        groupChartDataService.setEvidenceService(mockedEvidenceService);

        Map<String, Integer> result = groupChartDataService.getGroupCategoryInfo(testGroup1, startDate, endDate);
        assertEquals(1, result.get("Service"));
        assertEquals(1, result.get("Quantitative"));
        assertEquals(1, result.get("Qualitative"));

        Map<String, Integer> result2 = groupChartDataService.getGroupCategoryInfo(testGroup2, startDate, endDate);
        assertEquals(1, result2.get("Service"));
        assertEquals(1, result2.get("Quantitative"));
        assertEquals(1, result2.get("Qualitative"));
    }
}
