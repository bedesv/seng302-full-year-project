package nz.ac.canterbury.seng302.portfolio.service.project;

import nz.ac.canterbury.seng302.portfolio.model.project.Project;
import nz.ac.canterbury.seng302.portfolio.repository.project.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import nz.ac.canterbury.seng302.portfolio.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.*;

// more info here https://codebun.com/spring-boot-crud-application-using-thymeleaf-and-spring-data-jpa/

@Service
public class ProjectService {
    @Autowired
    private ProjectRepository repository;

    @Autowired
    private ProjectEditsService projectEditsService;
    private static final Logger PORTFOLIO_LOGGER = LoggerFactory.getLogger("com.portfolio");

    /**
     * Get list of all projects
     */
    public List<Project> getAllProjects() {
        return (List<Project>) repository.findAll();
    }

    /**
     * Get project by id
     */
    public Project getProjectById(Integer id) throws NoSuchElementException {
        Optional<Project> project = repository.findById(id);
        if (project.isPresent()) {
            return project.get();
        } else {
            throw new NoSuchElementException("Project not found");
        }
    }

    public Project saveProject(Project project) {
        if (!ValidationUtil.titleValid(project.getName())||!ValidationUtil.titleValid(project.getDescription())) {
            throw new IllegalArgumentException("Validation Error");
        } else {
            projectEditsService.refreshProject(project.getId());
            String message = "Project "+ project.getId() + " saved successfully";
            PORTFOLIO_LOGGER.info(message);
            return repository.save(project);
        }
    }

    public void deleteProjectById(int id) throws NoSuchElementException {
        try {
            repository.deleteById(id);
            projectEditsService.refreshProject(id);
            String message = "Project "+ id + " deleted successfully";
            PORTFOLIO_LOGGER.info(message);
        } catch (EmptyResultDataAccessException e) {
            String message = "Project "+ id + " not found to delete";
            PORTFOLIO_LOGGER.error(message);
            throw new NoSuchElementException("No project found to delete");
        }
    }

    /**
     * Method for updating an existing project
     * @param id of project
     * @param projectName name
     * @param projectDescription description
     * @param projectStartDate start date
     * @param projectEndDate end date
     * @return new projecct
     */
    public Project updateProject(int id, String projectName, String projectDescription, Date projectStartDate, Date projectEndDate) {
        Project existingProject = getProjectById(id);
        existingProject.setName(projectName);
        existingProject.setStartDate(projectStartDate);
        existingProject.setEndDate(projectEndDate);
        existingProject.setDescription(projectDescription);
        return saveProject(existingProject);
    }
}
