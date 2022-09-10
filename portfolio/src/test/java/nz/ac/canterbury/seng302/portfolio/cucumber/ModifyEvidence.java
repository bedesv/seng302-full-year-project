package nz.ac.canterbury.seng302.portfolio.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import nz.ac.canterbury.seng302.portfolio.model.evidence.Categories;
import nz.ac.canterbury.seng302.portfolio.model.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.model.project.Project;
import nz.ac.canterbury.seng302.portfolio.repository.project.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.service.evidence.EvidenceService;
import nz.ac.canterbury.seng302.portfolio.service.project.ProjectEditsService;
import nz.ac.canterbury.seng302.portfolio.service.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@CucumberContextConfiguration
@AutoConfigureTestDatabase
@SpringBootTest
public class ModifyEvidence {

    @Autowired
    private EvidenceService evidenceService = new EvidenceService();

    @Autowired
    private ProjectRepository repository;

    @Autowired
    private ProjectService  projectService= new ProjectService();

    @MockBean
    ProjectEditsService projectEditsService;

    private Project project;


    private static final String TEST_DESCRIPTION = "According to all know laws of aviation, there is no way a bee should be able to fly.";

    @Transactional
    @Given("I have created evidence with skills")
    public void iHaveCreatedEvidence() {
        project =  new Project("Project Name", "Test Project", Date.valueOf("2022-05-01"), Date.valueOf("2022-06-30"));
        repository.save(project);
        int projectId = projectService.getAllProjects().get(0).getId();
        Evidence evidence = new Evidence(0, projectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-05-8"));
        evidence.setSkills("Running Walking Diving");
        evidenceService.saveEvidence(evidence);
    }

    @Transactional
    @When("I modify a skill")
    public void iModifyASkill() {
        int projectId = projectService.getAllProjects().get(0).getId();
        String skillsToChange = "Walking walKing";
        evidenceService.updateEvidenceSkills(0, projectId, skillsToChange);
    }

    @Transactional
    @Then("The skill is changed within the project")
    public void theSkillIsChangedWithinTheProject() {
        int projectId = projectService.getAllProjects().get(0).getId();
        List<String> skills = new ArrayList<>();
        skills.add("Running");
        skills.add("walKing");
        skills.add("Diving");
        List<Evidence> evidenceList= evidenceService.getEvidenceByProjectId(projectId);
        List<String> skillList = evidenceList.get(0).getSkills();
        assertEquals(skills, skillList);
    }

    @Transactional
    @When("I remove a skill")
    public void iRemoveASkill() {
        List<Evidence> evidenceList = evidenceService.getEvidenceByProjectId(projectService.getAllProjects().get(0).getId());
        Evidence evidence = evidenceList.get(0);
        evidence.setSkills("Running Diving");
        evidenceService.saveEvidence(evidence);
    }

    @Transactional
    @Then("The skill is removed within the project")
    public void theSkillIsRemovedWithinTheProject() {
        int projectId = projectService.getAllProjects().get(0).getId();
        List<String> skills = new ArrayList<>();
        skills.add("Running");
        skills.add("Diving");
        List<Evidence> evidenceList= evidenceService.getEvidenceByProjectId(projectId);
        List<String> skillList = evidenceList.get(0).getSkills();
        assertEquals(skills, skillList);
    }

    @Transactional
    @Given("I have created evidence with categories")
    public void iHaveCreatedEvidenceWithCategories() {
        project =  new Project("Project Name", "Test Project", Date.valueOf("2022-05-01"), Date.valueOf("2022-06-30"));
        repository.save(project);
        int projectId = projectService.getAllProjects().get(0).getId();
        Evidence evidence = new Evidence(0, projectId, "Test Evidence", TEST_DESCRIPTION, Date.valueOf("2022-05-8"));
        Set<Categories> categories = new HashSet<>();
        categories.add(Categories.QUALITATIVE);
        categories.add(Categories.SERVICE);
        evidence.setCategories(categories);
        evidenceService.saveEvidence(evidence);
    }

    @Transactional
    @When("I remove a category")
    public void iRemoveACategory() {
        List<Evidence> evidenceList = evidenceService.getEvidenceByProjectId(projectService.getAllProjects().get(0).getId());
        Evidence evidence = evidenceList.get(0);
        Set<Categories> categories = new HashSet<>();
        categories.add(Categories.QUALITATIVE);
        evidence.setCategories(categories);
        evidenceService.saveEvidence(evidence);
    }

    @Transactional
    @Then("The category is removed within the project")
    public void theCategoryIsRemovedWithinTheProject() {
        int projectId = projectService.getAllProjects().get(0).getId();
        List<Categories> categories = new ArrayList<>();
        categories.add(Categories.QUALITATIVE);
        List<Evidence> evidenceList = evidenceService.getEvidenceByProjectId(projectId);
        List<Categories> categoriesList =  evidenceList.get(0).getCategories();
        assertEquals(categories, categoriesList);
    }

    @Transactional
    @When("I add a category")
    public void iAddACategory() {
        List<Evidence> evidenceList = evidenceService.getEvidenceByProjectId(projectService.getAllProjects().get(0).getId());
        Evidence evidence = evidenceList.get(0);
        Set<Categories> categories = new HashSet<>();
        categories.add(Categories.QUALITATIVE);
        categories.add(Categories.QUANTITATIVE);
        categories.add(Categories.SERVICE);
        evidence.setCategories(categories);
        evidenceService.saveEvidence(evidence);
    }

    @Transactional
    @Then("The category is added within the project")
    public void theCategoryIsAddedWithinTheProject() {
        int projectId = projectService.getAllProjects().get(0).getId();
        List<Categories> categories = new ArrayList<>();
        categories.add(Categories.QUALITATIVE);
        categories.add(Categories.QUANTITATIVE);
        categories.add(Categories.SERVICE);
        Collections.sort(categories);
        List<Evidence> evidenceList = evidenceService.getEvidenceByProjectId(projectId);
        List<Categories> categoriesList =  evidenceList.get(0).getCategories();
        assertEquals(categories, categoriesList);
    }
}
