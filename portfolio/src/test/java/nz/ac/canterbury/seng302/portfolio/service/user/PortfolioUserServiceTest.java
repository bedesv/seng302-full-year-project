package nz.ac.canterbury.seng302.portfolio.service.user;

import nz.ac.canterbury.seng302.portfolio.model.project.Project;
import nz.ac.canterbury.seng302.portfolio.model.user.PortfolioUser;
import nz.ac.canterbury.seng302.portfolio.repository.user.PortfolioUserRepository;
import nz.ac.canterbury.seng302.portfolio.service.project.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.user.PortfolioUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.parameters.P;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase
@SpringBootTest
class PortfolioUserServiceTest {

    @Autowired
    PortfolioUserService service;

    @Autowired
    ProjectService projectService;

    @Autowired
    PortfolioUserRepository repository;


    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    int userId = 1;



    //Test that querying a user which does not exist creates that user.
    @Test
    void queryNewUserTest() {
        service.getUserById(3);
        List<PortfolioUser> users = (List<PortfolioUser>) repository.findAll();
        assertEquals(1, users.size());
        service.getUserById(5);
        users = (List<PortfolioUser>) repository.findAll();
        assertEquals(2, users.size());
    }

    //Test that querying a user which does exist does not create that user.
    @Test
    void givenAUserHasBeenRemoved_queryUser() {
        service.getUserById(3);
        List<PortfolioUser> users = (List<PortfolioUser>) repository.findAll();
        assertEquals(1, users.size());
        service.getUserById(3);
        users = (List<PortfolioUser>) repository.findAll();
        assertEquals(1, users.size());
    }

    //Test that getting the default user list sort type works (should be by name)
    @Test
    void givenDefaultSortType_getUserList() {
        String resultSortType = service.getUserListSortType(3);
        assertEquals("name", resultSortType);
    }

    //Test that setting the user list sort type works
    @Test
    void givenValidSortType_getUserList() {
        String testSortType = "test sort type";
        service.setUserListSortType(3, testSortType);
        String resultSortType = service.getUserListSortType(3);
        assertEquals(testSortType, resultSortType);
    }

    //Test that getting the default user list sort type works (should be ascending)
    @Test
    void givenSortAscending_getUserList() {
        boolean resultSortType = service.isUserListSortAscending(3);
        assertTrue(resultSortType);
    }

    //Test that when no project is selected, the default project (id 1) is selected.
    @Test
    void givenNoProjectSelected_getProject() {
        service.getCurrentProject(3);
        assertEquals(1, service.getCurrentProject(3).getId());
    }

    //Test that when a project is selected, that project is selected.
    @Test
    void givenProjectSelected_getProject() {
        Project project = new Project();
        projectService.saveProject(project);
        service.setProject(3, project.getId());
        assertEquals(project.getId(), service.getCurrentProject(3).getId());
    }

    //Test that when a project is selected that does not exist, the default project (id 1) is selected instead.
    @Test
    void givenBadProjectSelected_getProject() {
        service.setProject(3, 9999);
        assertEquals(1, service.getCurrentProject(3).getId());
    }

    //Test that when all projects are deleted, the default project (id 1) is created and selected
    @Test
    void givenAllProjectsDeleted_getProject() {
        for (Project project : projectService.getAllProjects()) {
            projectService.deleteProjectById(project.getId());
        }
        Project userProject = service.getCurrentProject(3);
        List<Project> projects = projectService.getAllProjects();
        assertEquals(1, projects.size());
        assertEquals(projects.get(0).getId(), userProject.getId());
    }

    //Test that setting the user list sort type works
    @Test
    void givenSortAscending_setUserListSort() {
        service.setUserListSortAscending(3, false);
        boolean resultSortType = service.isUserListSortAscending(3);
        assertFalse(resultSortType);
    }

    ///////////////////////////////
    //PORTFOLIO USER SKILLS TESTS//
    ///////////////////////////////

    @Test
    @Transactional
    void givenPortfolioUserExists_addOneSkillToPortfolioUser(){
        PortfolioUser portfolioUser = new PortfolioUser(userId, "name", true);
        repository.save(portfolioUser);

        List<String> skills = new ArrayList<>();
        skills.add("skill");

        service.addPortfolioUserSkills(userId, skills);

        assertEquals(skills, service.getPortfolioUserSkills(userId));
    }

    @Test
    @Transactional
    void givenPortfolioUserExists_addMultipleSkillsToPortfolioUser(){
        PortfolioUser portfolioUser = new PortfolioUser(userId, "name", true);
        repository.save(portfolioUser);

        List<String> skills = new ArrayList<>();
        skills.add("one");
        skills.add("two");
        skills.add("three");

        service.addPortfolioUserSkills(userId, skills);

        assertEquals(skills, repository.findByUserId(userId).getSkills());
    }

    @Test
    @Transactional
    void givenPortfolioUserDoesNotExist_addOneSkillToPortfolioUser(){
        List<String> skills = new ArrayList<>();
        skills.add("skill");

        service.addPortfolioUserSkills(userId, skills);

        assertEquals(skills, repository.findByUserId(userId).getSkills());
    }

    @Test
    @Transactional
    void givenPortfolioUserDoesNotExist_addMultipleSkillsToPortfolioUser(){
        List<String> skills = new ArrayList<>();
        skills.add("one");
        skills.add("two");
        skills.add("three");

        service.addPortfolioUserSkills(userId, skills);

        assertEquals(skills, repository.findByUserId(userId).getSkills());
    }

    @Test
    @Transactional
    void givenPortfolioUserExists_andHasNoSkills_getSkills(){
        PortfolioUser portfolioUser = new PortfolioUser(userId, "name", true);
        repository.save(portfolioUser);

        assertTrue(service.getPortfolioUserSkills(userId).isEmpty());
    }

    @Test
    @Transactional
    void givenPortfolioUserExists_andHasOneSkill_getSkills(){
        PortfolioUser portfolioUser = new PortfolioUser(userId, "name", true);
        List<String> skills = new ArrayList<>();
        skills.add("skill");
        portfolioUser.addSkills(skills);
        repository.save(portfolioUser);

        assertEquals(skills, service.getPortfolioUserSkills(userId));
    }

    @Test
    @Transactional
    void givenPortfolioUserExists_andHasMultipleSkills_getSkills(){
        PortfolioUser portfolioUser = new PortfolioUser(userId, "name", true);
        List<String> skills = new ArrayList<>();
        skills.add("one");
        skills.add("two");
        skills.add("three");
        portfolioUser.addSkills(skills);
        repository.save(portfolioUser);

        assertEquals(skills, service.getPortfolioUserSkills(userId));
    }

    @Test
    @Transactional
    void givenPortfolioDoesNotExist_getSkills(){
        assertTrue(service.getPortfolioUserSkills(userId).isEmpty());
    }
}
