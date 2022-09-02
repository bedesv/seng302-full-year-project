package nz.ac.canterbury.seng302.portfolio.controller;


import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.portfolio.util.ValidationUtil;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The controller for handling get and post request on the edit user page to edit a user
 */
@Controller
public class EditUserController {

    @Autowired
    private UserAccountClientService userAccountClientService;

    private static final String EDIT_USER = "editUser";

    /**
     * Get mapping to open editUser page
     * @return the editUser page
     */
    @GetMapping("/editUser")
    public String editUser() {
        return EDIT_USER;
    }


    /**
     * Edit user post mapping,
     * called when user submits an edit user request
     * @param principal Authentication principal storing current user information
     * @param email (updated)
     * @param firstName (updated)
     * @param middleName (updated)
     * @param lastName (updated)
     * @param nickname (updated)
     * @param pronouns (updated)
     * @param bio (updated)
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return editUser page if unsuccessful, or profile page if successful
     */
    @PostMapping("/editUser")
    public String editUser(@AuthenticationPrincipal AuthState principal,
                           @RequestParam(name="email") String email,
                           @RequestParam(name="firstName") String firstName,
                           @RequestParam(name="middleName") String middleName,
                           @RequestParam(name="lastName") String lastName,
                           @RequestParam(name="nickname") String nickname,
                           @RequestParam(name="pronouns") String pronouns,
                           @RequestParam(name="bio") String bio,
                           Model model) {

        //get userId using the Authentication Principle
        int id = userAccountClientService.getUserId(principal);


        //Check for emojis early, prevents grpc error
        List<Boolean> validationResponses = userAccountClientService.validateAttributes(model, "", firstName, middleName, lastName, nickname, pronouns, bio, email);
        if (validationResponses.contains(false)){
            updateModel(model, ValidationUtil.stripName(firstName), ValidationUtil.stripName(middleName), ValidationUtil.stripName(lastName), ValidationUtil.stripTitle(nickname), ValidationUtil.stripTitle(bio),  ValidationUtil.stripTitle(email), ValidationUtil.stripTitle(pronouns));
            return EDIT_USER;
        }

        EditUserResponse editUserResponse;

        try {
            //Call the edit user via grpc with users validated params
            editUserResponse = userAccountClientService.editUser(id, firstName,
                    middleName, lastName, nickname, bio, pronouns, email);
            model.addAttribute("Response", editUserResponse.getMessage());
        } catch (Exception e){
            model.addAttribute("errorMessage", e);
            return EDIT_USER;
        }

        //if edit user was successful
        if (editUserResponse.getIsSuccess()){
            return "redirect:/profile";
        } else {
            //if edit user was unsuccessful
            updateModel(model, firstName, middleName, lastName, nickname, bio, email, pronouns);
            model.addAttribute("validationErrors", editUserResponse.getValidationErrorsList());
            return EDIT_USER;
        }
    }

    private void updateModel(Model model, String firstName, String middleName, String lastName, String nickname, String bio, String email, String pronouns){
        // Add attributes back into the page so the user doesn't have to enter them again
        model.addAttribute("editedFirstName", firstName);
        model.addAttribute("editedMiddleName", middleName);
        model.addAttribute("editedLastName", lastName);
        model.addAttribute("editedNickname", nickname);
        model.addAttribute("editedBio", bio);
        model.addAttribute("editedEmail", email);
        model.addAttribute("editedPronouns", pronouns);
    }
}