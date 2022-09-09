package nz.ac.canterbury.seng302.portfolio.controller.project;

import nz.ac.canterbury.seng302.portfolio.model.project.Project;
import nz.ac.canterbury.seng302.portfolio.model.project.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.project.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.project.SprintService;
import nz.ac.canterbury.seng302.portfolio.service.user.UserAccountClientService;
import nz.ac.canterbury.seng302.portfolio.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Controller for the edit sprint details page
 */
@Controller
public class EditSprintController {

    @Autowired
    UserAccountClientService userAccountClientService;
    @Autowired
    ProjectService projectService;
    @Autowired
    SprintService sprintService;

    private static final String PROJECTS_REDIRECT = "redirect:/projects";
    private static final String TIME_FORMAT = "yyyy-MM-dd";

    private static final String REDIRECT_PROJECT_DETAILS = "redirect:/projectDetails-";
    private static final String EDIT_SPRINT = "templatesProject/editSprint";


    /**
     * The get mapping to return the page to edit a sprint of a certain Project ID
     * @param principal Authentication principal storing current user information
     * @param projectIdString The Project ID of parent project of the sprint being displayed
     * @param sprintIdString The Sprint ID of the sprint being displayed
     * @param model ThymeLeaf model
     * @return The edit sprint page
     */
    @GetMapping("/editSprint-{sprintId}-{parentProjectId}")
    public String sprintForm(@AuthenticationPrincipal AuthState principal,
                             @PathVariable("parentProjectId") String projectIdString,
                             @PathVariable("sprintId") String sprintIdString,
                             Model model) {
        if (!userAccountClientService.isTeacher(principal)) {
            return REDIRECT_PROJECT_DETAILS + projectIdString;
        }

        int projectId;
        int sprintId;
        Sprint sprint;
        Project project;

        try {
            projectId = Integer.parseInt(projectIdString);
            sprintId = Integer.parseInt(sprintIdString);
        } catch (NumberFormatException e) {
            return PROJECTS_REDIRECT;
        }

        try {
            project = projectService.getProjectById(projectId);
        } catch (NoSuchElementException e) {
            return PROJECTS_REDIRECT;
        }

        //editing existing sprint
        if (sprintId != -1) {
            try {
                sprint = sprintService.getSprintById(sprintId);
            } catch (NoSuchElementException e) {
                return PROJECTS_REDIRECT;
            }
        //new sprint
        } else {
            sprint = sprintService.createDefaultSprint(projectId);
        }
        updateModel(model, project, sprint);
        return EDIT_SPRINT;
    }

    /**
     * Post request handler for adding/editing sprints
     * @param principal principal Authentication state of client
     * @param projectIdString The ID string of the parent project of the sprint
     * @param sprintIdString The ID string of the sprint being edited/created
     * @param sprintName The new sprint name
     * @param sprintStartDateString The new sprint start date
     * @param sprintEndDateString The new sprint end date
     * @param sprintDescription The new sprint description
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return Project details page or sprint from depending on if validation passes.
     */
    @PostMapping("/editSprint-{sprintId}-{parentProjectId}")
    public String sprintSave(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("parentProjectId") String projectIdString,
            @PathVariable("sprintId") String sprintIdString,
            @RequestParam(value="sprintName") String sprintName,
            @RequestParam(value="sprintStartDate") String sprintStartDateString,
            @RequestParam(value="sprintEndDate") String sprintEndDateString,
            @RequestParam(value="sprintDescription") String sprintDescription,
            Model model
    ) throws ParseException {
        if (!userAccountClientService.isTeacher(principal)) {
            return REDIRECT_PROJECT_DETAILS + projectIdString;
        }

        int projectId;
        int sprintId;
        Project project;

        Date sprintStartDate = new SimpleDateFormat(TIME_FORMAT).parse(sprintStartDateString);
        Date sprintEndDate = new SimpleDateFormat(TIME_FORMAT).parse(sprintEndDateString);
        try {
            projectId = Integer.parseInt(projectIdString);
            sprintId = Integer.parseInt(sprintIdString);
        } catch (NumberFormatException e) {
            return PROJECTS_REDIRECT ;
        }

        try {
            project = projectService.getProjectById(projectId);
        } catch (NoSuchElementException e) {
            return PROJECTS_REDIRECT;
        }
        model.addAttribute("projectId", projectId);

        //Date validation
        boolean validation = false;
        String startDateError = (sprintService.checkSprintStartDate(sprintId, projectId, sprintStartDate));
        String endDateError = (sprintService.checkSprintEndDate(sprintId, projectId, sprintEndDate));
        String datesError = (sprintService.checkSprintDates(sprintId, projectId, sprintStartDate, sprintEndDate));
        if (!Objects.equals(startDateError, "")) {
            model.addAttribute("startDateError", startDateError);
            validation = true;
        }
        if (!Objects.equals(endDateError, "")) {
            model.addAttribute("endDateError", endDateError);
            validation = true;
        }
        if (!Objects.equals(datesError, "")) {
            model.addAttribute("dateError", datesError);
            validation = true;
        }
        if (!ValidationUtil.titleValid(sprintName)){
            model.addAttribute("titleError", "Sprint name cannot contain special characters");
            validation = true;
        }

        if (!ValidationUtil.titleValid(sprintDescription)){
            model.addAttribute("descriptionError", "Sprint description cannot contain special characters");
            validation = true;
        }

        if (validation) {
            updateModel(model, project, new Sprint(projectId, ValidationUtil.stripTitle(sprintName), ValidationUtil.stripTitle(sprintDescription), sprintStartDate, sprintEndDate));
            return EDIT_SPRINT;
        }

        if (sprintId != -1) {
            try {
                sprintService.getSprintById(sprintId);
                sprintService.editSprint(projectId, sprintId, sprintName, sprintDescription, sprintStartDate, sprintEndDate);
            } catch (NoSuchElementException | IllegalArgumentException e) {
                Sprint sprint = sprintService.getSprintById(sprintId);
                sprint.setName(ValidationUtil.stripTitle(sprintName));
                sprint.setDescription(ValidationUtil.stripTitle(sprintDescription));
                sprint.setStartDate(sprintStartDate);
                sprint.setEndDate(sprintEndDate);
                updateModel(model, project, sprint);
                return EDIT_SPRINT;
            }
        } else {
            try {
                sprintService.createNewSprint(projectId, sprintName, sprintDescription, sprintStartDate, sprintEndDate);
            } catch (IllegalArgumentException e) {
                updateModel(model, project, new Sprint(projectId, ValidationUtil.stripTitle(sprintName), ValidationUtil.stripTitle(sprintDescription), sprintStartDate, sprintEndDate));
                return EDIT_SPRINT;
            }

        }
        return REDIRECT_PROJECT_DETAILS + projectIdString;
    }


    /**
     * The delete mapping for deleting sprints from a project
     * @param principal Authentication principal storing current user information
     * @param parentProjectId The parent project ID of the sprint being deleted
     * @param sprintId The sprint ID of the sprint being delted
     * @return The projects page
     */
    @DeleteMapping(value="/editSprint-{sprintId}-{parentProjectId}")
    public String deleteSprint(@AuthenticationPrincipal AuthState principal,
                                    @PathVariable("parentProjectId") String parentProjectId,
                                    @PathVariable("sprintId") String sprintId) {
        if (!userAccountClientService.isTeacher(principal)) {
            return REDIRECT_PROJECT_DETAILS + parentProjectId;
        }

        sprintService.deleteSprint(Integer.parseInt(parentProjectId), Integer.parseInt(sprintId));
        return REDIRECT_PROJECT_DETAILS + parentProjectId;
    }


    /**
     * Abstracted these additions instead of being repeated three times
     * @param model global model
     * @param project current project
     * @param sprint selected sprint
     */
    private void updateModel(Model model, Project project, Sprint sprint) {
        model.addAttribute("projectId", project.getId());
        // Add sprint details to model
        model.addAttribute("sprintName", sprint.getName());
        model.addAttribute("sprintLabel", sprint.getLabel());
        model.addAttribute("sprintDescription", sprint.getDescription());
        model.addAttribute("sprintStartDate", Project.dateToString(sprint.getStartDate(), TIME_FORMAT));
        model.addAttribute("sprintEndDate", Project.dateToString(sprint.getEndDate(), TIME_FORMAT));
        // Add date boundaries for sprint to model
        model.addAttribute("minSprintStartDate", Project.dateToString(project.getStartDate(), TIME_FORMAT));
        model.addAttribute("maxSprintEndDate", Project.dateToString(project.getEndDate(), TIME_FORMAT));
    }
}
