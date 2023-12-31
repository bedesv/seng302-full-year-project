package nz.ac.canterbury.seng302.portfolio.controller.project;

import nz.ac.canterbury.seng302.portfolio.model.project.Milestone;
import nz.ac.canterbury.seng302.portfolio.model.project.Project;
import nz.ac.canterbury.seng302.portfolio.service.project.MilestoneService;
import nz.ac.canterbury.seng302.portfolio.service.project.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.user.UserAccountClientService;
import nz.ac.canterbury.seng302.portfolio.util.ValidationUtil;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Controller for adding/editing deadlines
 */
@Controller
public class EditMilestoneController {

    @Autowired
    UserAccountClientService userAccountClientService;

    @Autowired
    ProjectService projectService;

    @Autowired
    MilestoneService milestoneService;

    private static final String TIME_FORMAT = "yyyy-MM-dd";
    private static final String REDIRECT_PROJECTS = "redirect:/projects";

    private static final String REDIRECT_PROJECT_DETAILS = "redirect:/projectDetails-";
    private static final String EDIT_MILESTONE = "templatesProject/editMilestone";

    /**
     * The get mapping to return the page with the form to add/edit milestones
     * @param principal Authentication principle
     * @param parentProjectId The parent project ID
     * @param milestoneIdString milestone current ID or -1 for a new deadline
     * @param model The model
     */
    @GetMapping("/editMilestone-{milestoneId}-{parentProjectId}")
    public String milestoneForm(@AuthenticationPrincipal AuthState principal,
                                @PathVariable("parentProjectId") String parentProjectId,
                                @PathVariable("milestoneId") String milestoneIdString,
                                Model model) {

        if (!userAccountClientService.isTeacher(principal)) {
            return REDIRECT_PROJECT_DETAILS + parentProjectId;
        }

        int projectId;
        int milestoneId;

        try {
            projectId = Integer.parseInt(parentProjectId);
            milestoneId = Integer.parseInt(milestoneIdString);
        } catch (NumberFormatException e) {
            return REDIRECT_PROJECTS;
        }
        Project project = projectService.getProjectById(projectId);

        //Create the default date for a new milestone. current date it falls within project start and finish otherwise project start date
        Date milestoneDate;
        Date currentDate = new Date();
        if(currentDate.after(project.getStartDate()) && currentDate.before(project.getEndDate())) {
            milestoneDate = currentDate;
        } else {
            milestoneDate = project.getStartDate();
        }

        //Create new or get existing milestone
        Milestone milestone;
        if (milestoneId != -1) {
            milestone = milestoneService.getMilestoneById(milestoneId);
        } else {
            milestone = new Milestone(projectId, "New Milestone", milestoneDate);
        }
        updateModel(model, project, milestone);
        return EDIT_MILESTONE;
    }

    /**
     * The post mapping for submitting the add/edit milestone form
     * @param principle Authentication principle
     * @param projectIdString The project ID string representing the parent project ID
     * @param milestoneIdString The Milestone ID string representing the milestone, -1 for a new deadline
     * @param milestoneName The new/edited/existing milestone name
     * @param milestoneDateString The new/edited/existing milestone date
     * @param model The model
     */
    @PostMapping("/editMilestone-{milestoneId}-{parentProjectId}")
    public String submitForm(
            @AuthenticationPrincipal AuthState principle,
            @PathVariable("parentProjectId") String projectIdString,
            @PathVariable("milestoneId") String milestoneIdString,
            @RequestParam(value = "milestoneName") String milestoneName,
            @RequestParam(value = "milestoneDate") String milestoneDateString,
            Model model) throws ParseException {

        if (!userAccountClientService.isTeacher(principle)) {
            return REDIRECT_PROJECT_DETAILS + projectIdString;
        }

        Date milestoneDate = new SimpleDateFormat(TIME_FORMAT).parse(milestoneDateString);

        int milestoneId;
        int projectId;
        try {
            projectId = Integer.parseInt(projectIdString);
            milestoneId = Integer.parseInt(milestoneIdString);
        } catch (NumberFormatException e) {
            return REDIRECT_PROJECTS;
        }

        //check if creating or editing existing deadline
        if (milestoneId == -1) {
            try {
                milestoneService.createNewMilestone(projectId, milestoneName, milestoneDate);
            } catch (IllegalArgumentException e) {
                updateModel(model, projectService.getProjectById(projectId), new Milestone(projectId, ValidationUtil.stripTitle(milestoneName), milestoneDate));
                model.addAttribute("titleError", "Milestone name cannot contain special characters");
                return EDIT_MILESTONE;
            }
        } else {
            try {
                milestoneService.updateMilestone(projectId, milestoneId, milestoneName, milestoneDate);
            } catch (IllegalArgumentException e) {
                Milestone milestone = milestoneService.getMilestoneById(milestoneId);
                milestone.setMilestoneName(ValidationUtil.stripTitle(milestoneName));
                milestone.setMilestoneDate(milestoneDate);
                updateModel(model, projectService.getProjectById(projectId), milestone);
                model.addAttribute("titleError", "Milestone name cannot contain special characters");
                return EDIT_MILESTONE;
            }
        }
        return REDIRECT_PROJECT_DETAILS + projectIdString;
    }

    /**
     * Delete mapping for deleting milestones
     * @param principal Authentication principle
     * @param parentProjectId The project ID string representing the parent project ID
     * @param milestoneId The Milestone ID string representing the milestone
     * @return Project details page
     */
    @DeleteMapping("/editMilestone-{milestoneId}-{parentProjectId}")
    public String deleteMilestone(@AuthenticationPrincipal AuthState principal,
                                    @PathVariable("parentProjectId") String parentProjectId,
                                    @PathVariable("milestoneId") String milestoneId) {
        if (!userAccountClientService.isTeacher(principal)) {
            return REDIRECT_PROJECT_DETAILS + parentProjectId;
        }

        milestoneService.deleteMilestoneById(Integer.parseInt(milestoneId));
        return REDIRECT_PROJECT_DETAILS + parentProjectId;
    }

    /**
     * Abstracted these additions instead of being repeated three times
     * @param model
     * @param project
     * @param milestone
     */
    private void updateModel(Model model, Project project, Milestone milestone){
        model.addAttribute("projectId", project.getId());
        model.addAttribute("milestone", milestone);
        model.addAttribute("milestoneName", milestone.getMilestoneName());
        model.addAttribute("milestoneDate", Project.dateToString(milestone.getMilestoneDate(), TIME_FORMAT));
        model.addAttribute("minMilestoneDate", Project.dateToString(project.getStartDate(), TIME_FORMAT));
        model.addAttribute("maxMilestoneDate", Project.dateToString(project.getEndDate(), TIME_FORMAT));
    }

}