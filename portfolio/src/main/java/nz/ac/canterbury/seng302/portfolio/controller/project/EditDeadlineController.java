package nz.ac.canterbury.seng302.portfolio.controller.project;

import nz.ac.canterbury.seng302.portfolio.model.project.Deadline;
import nz.ac.canterbury.seng302.portfolio.model.project.Project;
import nz.ac.canterbury.seng302.portfolio.service.project.DeadlineService;
import nz.ac.canterbury.seng302.portfolio.service.project.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.user.UserAccountClientService;
import nz.ac.canterbury.seng302.portfolio.util.ValidationUtil;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Controller for adding/editing deadlines
 */
@Controller
public class EditDeadlineController {

    @Autowired
    UserAccountClientService userAccountClientService;

    @Autowired
    ProjectService projectService;

    @Autowired
    DeadlineService deadlineService;

    private static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm";
    private static final String REDIRECT_PROJECTS = "redirect:/projects";

    private static final String REDIRECT_PROJECT_DETAILS = "redirect:/projectDetails-";
    private static final String EDIT_DEADLINE = "templatesProject/editDeadline";

    /**
     * The get mapping to return the page with the form to add/edit deadlines
     * @param principal Authentication principle
     * @param parentProjectId The parent project ID
     * @param deadlineId Deadline ID, -1 for a new deadline
     * @param model The model
     */
    @GetMapping("/editDeadline-{deadlineId}-{parentProjectId}")
    public String deadLineForm(@AuthenticationPrincipal AuthState principal,
                            @PathVariable("parentProjectId") String parentProjectId,
                            @PathVariable("deadlineId") String deadlineId,
                            Model model) {
        if (!userAccountClientService.isTeacher(principal)) {
            return REDIRECT_PROJECT_DETAILS + parentProjectId;
        }

        int projectId = Integer.parseInt(parentProjectId);
        Project project = projectService.getProjectById(projectId);


        Deadline deadline;

        Date deadlineDate;
        Date currentDate = new Date();
        if(currentDate.after(project.getStartDate()) && currentDate.before(project.getEndDate())) {
            deadlineDate = currentDate;
        } else {
            deadlineDate = project.getStartDate();
        }

        if (Integer.parseInt(deadlineId) != -1) {
            deadline = deadlineService.getDeadlineById(Integer.parseInt(deadlineId));
        } else {
            deadline = new Deadline(projectId, "Deadline name", deadlineDate);
        }
        updateModel(model, project ,deadline);
        return EDIT_DEADLINE;
    }

    /**
     * The post mapping for submitting the add/edit deadline form
     * @param principle Authentication principle
     * @param projectIdString The project ID string representing the parent project ID
     * @param deadlineIdString The deadline ID string representing the deadline, -1 for a new deadline
     * @param deadlineName The new deadline name
     * @param deadlineDateString The new deadline date
     * @param model The model
     */
    @PostMapping("/editDeadline-{deadlineId}-{parentProjectId}")
    public String submitForm(
            @AuthenticationPrincipal AuthState principle,
            @PathVariable("parentProjectId") String projectIdString,
            @PathVariable("deadlineId") String deadlineIdString,
            @RequestParam(value="deadlineName") String deadlineName,
            @RequestParam(value="deadlineDate") String deadlineDateString,
            Model model) throws ParseException {

        if (!userAccountClientService.isTeacher(principle)) {
            return REDIRECT_PROJECT_DETAILS + projectIdString;
        }

        int deadlineId;
        int projectId;

        Timestamp deadlineDateTimeStamp = Timestamp.valueOf(deadlineDateString.replace("T", " ") +":00");
        Date deadlineDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").parse(Deadline.dateToString(deadlineDateTimeStamp));

        try {
            deadlineId = Integer.parseInt(deadlineIdString);
            projectId = Integer.parseInt(projectIdString);
        } catch (NumberFormatException e) {
            return REDIRECT_PROJECTS;
        }

        //check if creating or editing existing deadline
        if (deadlineId == -1) {
            try {
                deadlineService.createNewDeadline(projectId, deadlineName, deadlineDate);
            } catch (IllegalArgumentException e) {
                Deadline deadline = new Deadline(projectId, ValidationUtil.stripTitle(deadlineName), deadlineDate);
                updateModel(model, projectService.getProjectById(projectId), deadline);
                model.addAttribute("titleError", "Deadline name cannot contain special characters");
                return EDIT_DEADLINE;
            }
        } else {
            try {
                deadlineService.updateDeadline(projectId, deadlineId, deadlineName, deadlineDate);
            } catch (IllegalArgumentException e) {
                Deadline deadline = deadlineService.getDeadlineById(deadlineId);
                deadline.setDeadlineName(ValidationUtil.stripTitle(deadlineName));
                deadline.setDeadlineDate(deadlineDate);
                updateModel(model, projectService.getProjectById(projectId), deadline);
                model.addAttribute("titleError", "Deadline name cannot contain special characters");
                return EDIT_DEADLINE;
            }
        }
        return REDIRECT_PROJECT_DETAILS + projectIdString;
    }


    @DeleteMapping("/editDeadline-{deadlineId}-{parentProjectId}")
    public String deleteProjectById(@AuthenticationPrincipal AuthState principal,
                                    @PathVariable("parentProjectId") String parentProjectId,
                                    @PathVariable("deadlineId") String deadlineId) {
        if (!userAccountClientService.isTeacher(principal)) {
            return REDIRECT_PROJECT_DETAILS + parentProjectId;
        }

        deadlineService.deleteDeadlineById(Integer.parseInt(deadlineId));
        return REDIRECT_PROJECT_DETAILS + parentProjectId;
    }

    /**
     * Abstracted these additions instead of being repeated three times
     * @param model
     * @param project
     * @param deadline
     */
    private void updateModel(Model model, Project project, Deadline deadline){
        model.addAttribute("projectId", project.getId());
        model.addAttribute("deadline", deadline);
        model.addAttribute("deadlineName", deadline.getDeadlineName());
        model.addAttribute("deadlineDate", Project.dateToString(deadline.getDeadlineDate(), TIME_FORMAT));
        model.addAttribute("minDeadlineDate", Project.dateToString(project.getStartDate(), TIME_FORMAT));
        model.addAttribute("maxDeadlineDate", Project.dateToString(project.getEndDate(), TIME_FORMAT));
        model.addAttribute("maxDeadlineDate", Project.dateToString(project.getEndDate(), TIME_FORMAT));
    }


}
