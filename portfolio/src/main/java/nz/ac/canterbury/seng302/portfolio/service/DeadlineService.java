package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.Deadline;
import nz.ac.canterbury.seng302.portfolio.model.DeadlineRepository;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

// more info here https://codebun.com/spring-boot-crud-application-using-thymeleaf-and-spring-data-jpa/

@Service
public class DeadlineService {

    @Autowired
    private DeadlineRepository deadlineRepository;

    @Autowired
    private ProjectService deadlineProjectService;

    public List<Deadline> getAllDeadlines() {
        return (List<Deadline>) deadlineRepository.findAll();
    }

    public Deadline getDeadlineById(Integer deadlineId) throws Exception {
        Optional<Deadline> deadline = deadlineRepository.findById(deadlineId);
        if (deadline.isPresent()) {
            return deadline.get();
        } else {
            throw new Exception("Deadline not found");
        }
    }

    public List<Deadline> getByDeadlineParentProjectId(int deadlineProjectId) {
        return deadlineRepository.findByDeadlineParentProjectId(deadlineProjectId);
    }

    public Map<Integer, List<Deadline>> getAllByDeadlineParentProjectId() {
        List<Project> deadlineProjects = deadlineProjectService.getAllProjects();

        Map<Integer, List<Deadline>> deadlinesByParentProject = new HashMap<>();
        for (Project deadlineProject : deadlineProjects) {
            int deadlineId = deadlineProject.getId();
            deadlinesByParentProject.put(deadlineId, getByDeadlineParentProjectId(deadlineId));
        }
        return deadlinesByParentProject;
    }

    public Deadline saveDeadline(Deadline deadline) {
        return deadlineRepository.save(deadline);
    }
}
