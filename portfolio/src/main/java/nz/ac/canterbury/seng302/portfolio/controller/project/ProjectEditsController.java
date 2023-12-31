package nz.ac.canterbury.seng302.portfolio.controller.project;

import nz.ac.canterbury.seng302.portfolio.service.project.ProjectEditsService;
import nz.ac.canterbury.seng302.portfolio.service.AuthenticateClientService;
import nz.ac.canterbury.seng302.portfolio.service.project.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.user.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

/**
 * Class which allows users to register and receive edit notifications for projects.
 */
@RestController
public class ProjectEditsController {

    @Autowired
    AuthenticateClientService authenticateClientService;

    @Autowired
    UserAccountClientService userAccountClientService;

    @Autowired
    ProjectService projectService;

    @Autowired
    private ProjectEditsService projectEditsService;

    /**
     * Returns a list of users to send to the frontend, in JSON form.
     * Will only get relevant edits.
     * Also sends a single parameter 'refresh' to the user telling them if they need to refresh their page.
     * This is if the page has changed since the user last called this method.
     * Teachers and above get both, students only get the refresh notification.
     * @param principal The user's authentication
     * @param id The project the users wishes to get edits for
     * @return A JSON string to send to the frontend representing the edits a user is interested in.
     */
    @GetMapping("projects-editStatus")
    public String projectEditing(@AuthenticationPrincipal AuthState principal,
                                 @RequestParam String id) {
        boolean isAuthenticated = authenticateClientService.checkAuthState().getIsAuthenticated();
        boolean isTeacher = userAccountClientService.isTeacher(principal) && isAuthenticated;
        int userId = userAccountClientService.getUserId(principal);

        int projectId;
        try {
            projectId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return ""; // If project id is not an integer, the request was invalid, and we just return the empty string
        }

        if (isAuthenticated && userId != -100) {
            if (isTeacher) {
                return projectEditsService.getEdits(projectId, userId);
            } else {
                // If the user is not a teacher, they don't need to get edit notifications, they just need to be told when to refresh.
                return "{" + projectEditsService.getShouldRefresh(projectId, userId) + "}";
            }
        } else {
            return ""; //Return empty string as user is not authenticated
        }
    }

    /**
     * Allows teachers or above to register an edit notification to show to other users who are interested in the project.
     * @param principal The user's authentication
     * @param id The project the users wishes to register an edit for
     */
    @PostMapping("/projects-editing")
    public void isEditingProject(@AuthenticationPrincipal AuthState principal,
                                 @RequestParam String id,
                                 @RequestParam String name,
                                 @RequestParam String isEditing) {
        boolean isTeacher = userAccountClientService.isTeacher(principal) && authenticateClientService.checkAuthState().getIsAuthenticated();
        int userId = userAccountClientService.getUserId(principal);
        int projectId;
        try {
            projectId = Integer.parseInt(id);
        } catch (NumberFormatException | NoSuchElementException e) {
            return;
            // If project id is not an integer or does not correspond to a project, the request was invalid, so we return
        }
        if (isTeacher && userId != -100) {
            String editString = userAccountClientService.getUserAccountById(userId).getFirstName();
            if(Integer.parseInt(isEditing) == -1) {
                editString += " is creating a " + name.toLowerCase();
            } else {
                editString += " is editing " + name;
            }
            projectEditsService.newEdit(projectId, userId, editString);
        }
    }

}
