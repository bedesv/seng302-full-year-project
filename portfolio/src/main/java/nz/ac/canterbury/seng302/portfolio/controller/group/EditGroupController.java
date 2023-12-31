package nz.ac.canterbury.seng302.portfolio.controller.group;

import nz.ac.canterbury.seng302.portfolio.model.group.Group;
import nz.ac.canterbury.seng302.portfolio.service.group.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.user.PortfolioUserService;
import nz.ac.canterbury.seng302.portfolio.service.user.UserAccountClientService;
import nz.ac.canterbury.seng302.portfolio.util.ValidationUtil;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.CreateGroupResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.ModifyGroupDetailsResponse;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class EditGroupController {

    @Autowired
    private UserAccountClientService userAccountClientService;

    @Autowired
    private GroupsClientService groupsClientService;

    @Autowired
    private GroupsController groupsController;
    @Autowired
    private PortfolioUserService portfolioUserService;

    private static final String GROUPS_REDIRECT = "redirect:/groups";
    private static final String ADD_EDIT_GROUP = "templatesGroup/addEditGroup";

    /**
     * The get mapping to return the page to add/edit a group
     * @param principal Authentication principal storing current user information
     * @param groupId The id in of the group to be edited
     * @param model ThymeLeaf model
     * @return The edit group page with the relevant attributes injected
     */
    @GetMapping("/editGroup-{groupId}")
    public String editGroup(@AuthenticationPrincipal AuthState principal,
                            @PathVariable("groupId") String groupId,
                            Model model) {

        int userId = userAccountClientService.getUserId(principal);
        //Check User is a teacher otherwise return to project page
        if (!userAccountClientService.isTeacher(principal) && !groupsClientService.userInGroup(Integer.parseInt(groupId), userId)) {
            return GROUPS_REDIRECT;
        }

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
        model.addAttribute("groupId", Integer.parseInt(groupId));
        model.addAttribute("groupShortName", group.getShortName());
        model.addAttribute("groupLongName", group.getLongName());
        return ADD_EDIT_GROUP;
    }

    /**
     * Post mapping to save edits or new groups
     * @param principal Authentication principal storing current user information
     * @param groupIdString The id in string form of the group to be updated
     * @param groupShortName The new/updated short name of the group to be updated
     * @param groupLongName The new/updated long name of the group to be updated
     * @param model ThymeLeaf model
     * @return a redirect to either the edit group page or the main groups page
     */
    @PostMapping("/editGroup-{id}")
    public String saveGroupEdits(@AuthenticationPrincipal AuthState principal,
                              @PathVariable("id") String groupIdString,
                              @RequestParam("groupShortName") String groupShortName,
                              @RequestParam("groupLongName") String groupLongName,
                              Model model) {

        int userId = userAccountClientService.getUserId(principal);
        //Check if it is a teacher making the request
        if (!userAccountClientService.isTeacher(principal)&& !groupsClientService.userInGroup(Integer.parseInt(groupIdString), userId)) {
            return GROUPS_REDIRECT;
        }

        int parentProjectId = portfolioUserService.getUserById(userId).getCurrentProject();
        int groupId;

        try {
            groupId = Integer.parseInt(groupIdString);
        } catch (NumberFormatException e) {
            return GROUPS_REDIRECT;
        }

        List<Boolean> validFields = new ArrayList<>();
        validFields.add(ValidationUtil.validAttribute(model, "shortName", "Short name" ,groupShortName));
        validFields.add(ValidationUtil.validAttribute(model, "longName", "Long name", groupShortName));
        if (validFields.contains(false)){
            updateModel(model, groupId, ValidationUtil.stripTitle(groupShortName), ValidationUtil.stripTitle(groupLongName));
            return ADD_EDIT_GROUP;
        }

        List<ValidationError> validationErrorList;
        boolean responseSuccess;

        if (groupId == -1) {
            CreateGroupResponse response = groupsClientService.createGroup(groupShortName, groupLongName, parentProjectId);
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

            updateModel(model, groupId, groupShortName, groupLongName);
            return ADD_EDIT_GROUP;
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
    public String deleteGroupById(@AuthenticationPrincipal AuthState principal, @PathVariable("id") String groupId) {
        if (userAccountClientService.isTeacher(principal)) {
            int id = Integer.parseInt(groupId);
            try {
                groupsClientService.deleteGroupById(id);
            } catch (Exception ignored) {
                // Don't do anything if delete fails
            }
        }
        return GROUPS_REDIRECT;
    }

    /**
     * Update model
     * @param model global model
     * @param groupId of group
     * @param groupShortName of group
     * @param groupLongName of group
     */
    private void updateModel(Model model, int groupId, String groupShortName, String groupLongName){
        //Add event details to model so the user doesn't have to enter them again
        model.addAttribute("groupId", groupId);
        model.addAttribute("groupShortName", groupShortName);
        model.addAttribute("groupLongName", groupLongName);
    }
}
