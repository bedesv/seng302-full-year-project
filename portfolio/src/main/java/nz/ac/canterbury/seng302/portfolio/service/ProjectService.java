package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.ProjectRepository;
import org.hibernate.annotations.NotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// more info here https://codebun.com/spring-boot-crud-application-using-thymeleaf-and-spring-data-jpa/

@Service
public class ProjectService {
    @Autowired
    private ProjectRepository repository;

    /**
     * Get list of all projects
     */
    public List<Project> getAllProjects() {
        List<Project> list = (List<Project>) repository.findAll();
        return list;
    }

    /**
     * Get project by id
     */
    public Project getProjectById(Integer id) throws Exception {

        Optional<Project> project = repository.findById(id);
        if(project.isPresent()) {
            return project.get();
        }
        else
        {
            throw new Exception("Project not found");
        }
    }

    public void saveProject(Project project) {
        Project projectSaved = repository.save(project);

    }

    public void deleteProjectById(int id) throws Exception {
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new Exception("No project found to delete");
        }

    }
}
