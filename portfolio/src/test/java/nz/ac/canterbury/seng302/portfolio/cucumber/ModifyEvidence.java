package nz.ac.canterbury.seng302.portfolio.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
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
import java.util.ArrayList;
import java.util.List;

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
        evidence.setSkills(evidenceService.stripSkills(evidence.getSkills()));
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
}
