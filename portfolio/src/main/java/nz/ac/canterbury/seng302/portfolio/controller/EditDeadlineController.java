package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Deadline;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.User;
import nz.ac.canterbury.seng302.portfolio.service.DeadlineService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    private String timeFormat = "yyyy-MM-dd";
    private String redirectToProjects = "redirect:/projects";

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
                            Model model) throws Exception {
        if (!userAccountClientService.isTeacher(principal)) {
            return redirectToProjects;
        }
        int userId = Integer.parseInt(principal.getClaimsList().stream()
                .filter(claim -> claim.getType().equals("nameid"))
                .findFirst()
                .map(ClaimDTO::getValue)
                .orElse("-100"));
        User user = userAccountClientService.getUserAccountById(userId);
        model.addAttribute("user", user);

        int projectId = Integer.parseInt(parentProjectId);
        Project project = projectService.getProjectById(projectId);
        model.addAttribute("projectId", project.getId());

        Deadline deadline;
        if (Integer.parseInt(deadlineId) != -1) {
            deadline = deadlineService.getDeadlineById(Integer.parseInt(deadlineId));
        } else {
            deadline = new Deadline(projectId, "Deadline name", project.getEndDate());
        }
        model.addAttribute("deadline", deadline);
        model.addAttribute("deadlineDate", Project.dateToString(deadline.getDeadlineDate(), timeFormat));
        model.addAttribute("minDeadlineDate", Project.dateToString(project.getStartDate(), timeFormat));
        model.addAttribute("maxDeadlineDate", Project.dateToString(project.getEndDate(), timeFormat));
        return "editDeadline";
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
            Model model) throws Exception {

        if (!userAccountClientService.isTeacher(principle)) {
            return redirectToProjects;
        }

        int deadlineId;
        int projectId;

        Date deadlineDate = new SimpleDateFormat(timeFormat).parse(deadlineDateString);
        try {
            deadlineId = Integer.parseInt(deadlineIdString);
            projectId = Integer.parseInt(projectIdString);
        } catch (NumberFormatException e) {
            return redirectToProjects;
        }

        //check if creating or editing existing deadline
        if (deadlineId == -1) {
            try {
                deadlineService.createNewDeadline(projectId, deadlineName, deadlineDate);
            } catch (UnsupportedOperationException e) {
                return("redirect:/editDeadline-{deadlineId}-{parentProjectId}");
            }
        } else {
            try {
                deadlineService.updateDeadline(projectId, deadlineId, deadlineName, deadlineDate);
            } catch(UnsupportedOperationException e) {
                return("redirect:/editDeadline-{deadlineId}-{parentProjectId}");
            }
        }
        return "redirect:/projectDetails-" + projectIdString;
    }



}
