package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.model.Group;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.User;
import nz.ac.canterbury.seng302.portfolio.service.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.CreateGroupResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.DeleteGroupResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.ModifyGroupDetailsResponse;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class EditGroupController {

    @Autowired
    private UserAccountClientService userAccountClientService;

    @Autowired
    private GroupsClientService groupsClientService;

    private static final String GROUPS_REDIRECT = "redirect:/groups";

    /**
     * The get mapping to return the page to add/edit an group
     */
    @GetMapping("/editGroup-{groupId}")
    public String editGroup(@AuthenticationPrincipal AuthState principle,
                            @PathVariable("groupId") String groupId,
                            Model model) {

        //Check User is a teacher otherwise return to project page
        if (!userAccountClientService.isTeacher(principle)) {
            return "redirect:/projects";
        }

        // Add user details to model for displaying in top banner
        int userId = userAccountClientService.getUserId(principle);
        User user = userAccountClientService.getUserAccountById(userId);
        model.addAttribute("user", user);

        Group group;
        //Check if it is existing or new group
        if (Integer.parseInt(groupId) != -1) {
            group = new Group(groupsClientService.getGroupDetailsById(Integer.parseInt(groupId)));
        } else {
            //Create new group
            group = new Group();
            group.setShortName("Short Name");
            group.setLongName("Long Name");
        }

        //Add event details to model
        model.addAttribute("groupShortName", group.getShortName());
        model.addAttribute("groupLongName", group.getLongName());

        return "editGroup";
    }

    @PostMapping("/editGroup-{id}")
    public String saveGroupEdits(@AuthenticationPrincipal AuthState principle,
                              @PathVariable("id") String groupIdString,
                              @RequestParam("groupShortName") String groupShortName,
                              @RequestParam("groupLongName") String groupLongName,
                              Model model) {
        //Check if it is a teacher making the request
        if (!userAccountClientService.isTeacher(principle)) {
            return GROUPS_REDIRECT;
        }

        int userId = userAccountClientService.getUserId(principle);
        User user = userAccountClientService.getUserAccountById(userId);

        int groupId;

        try {
            groupId = Integer.parseInt(groupIdString);
        } catch (NumberFormatException e) {
            return GROUPS_REDIRECT;
        }

        List<ValidationError> validationErrorList;
        boolean responseSuccess;

        if (groupId == -1) {
            CreateGroupResponse response = groupsClientService.createGroup(groupShortName, groupLongName);
            responseSuccess = response.getIsSuccess();
            validationErrorList = response.getValidationErrorsList();
        } else {
            Group group = new Group(groupsClientService.getGroupDetailsById(groupId));
            group.setGroupId(groupId);
            group.setShortName(groupShortName);
            group.setLongName(groupLongName);
            ModifyGroupDetailsResponse response = groupsClientService.updateGroupDetails(group);
            responseSuccess = response.getIsSuccess();
            validationErrorList = response.getValidationErrorsList();

        }
        if (!responseSuccess) {
            for (ValidationError error : validationErrorList) {
                if (error.getFieldName().equals("shortName")) {
                    model.addAttribute("shortNameErrorMessage", error.getErrorText());
                } else if (error.getFieldName().equals("longName")) {
                    model.addAttribute("longNameErrorMessage", error.getErrorText());
                }
            }

            // Add user details to model for displaying in top banner
            model.addAttribute("user", user);

            //Add event details to model so the user doesn't have to enter them again
            model.addAttribute("groupId", groupId);
            model.addAttribute("groupShortName", groupShortName);
            model.addAttribute("groupLongName", groupLongName);

            return "editGroup";
        } else {
            return GROUPS_REDIRECT;
        }

    }

    /**
     * Delete endpoint for groups. Takes id parameter from http request and deletes the corresponding group from
     * the database.
     * @param groupId ID of the project to be deleted from the database.
     * @return Redirects back to the GET mapping for /groups.
     */
    @DeleteMapping(value="/editGroup-{id}")
    public String deleteGroupById(@AuthenticationPrincipal AuthState principle, @PathVariable("id") String groupId) {
        if (userAccountClientService.isTeacher(principle)) {
            int id = Integer.parseInt(groupId);
            try {
                groupsClientService.deleteGroupById(id);
            } catch (Exception ignored) {
                // Don't do anything if delete fails
            }
        }
        return GROUPS_REDIRECT;
    }
}
