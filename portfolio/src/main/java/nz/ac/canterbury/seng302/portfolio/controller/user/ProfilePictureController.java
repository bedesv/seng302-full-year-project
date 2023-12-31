package nz.ac.canterbury.seng302.portfolio.controller.user;

import com.google.protobuf.Timestamp;
import nz.ac.canterbury.seng302.portfolio.model.user.User;
import nz.ac.canterbury.seng302.portfolio.service.user.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import nz.ac.canterbury.seng302.shared.identityprovider.DeleteUserProfilePhotoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Controller
public class ProfilePictureController {

    @Autowired
    private UserAccountClientService userAccountClientService;

    private static final String NAME_ID_CLAIM_TYPE = "nameid";

    private static final String PROFILE_PICTURE_REDIRECT = "templatesUser/addProfilePicture";

    /**
     * Get mapping to open addProfilePicture page
     * @return the addProfilePicture page
     */
    @GetMapping("/addProfilePicture")
    public String addProfilePicture() {
        return PROFILE_PICTURE_REDIRECT;
    }


    /**
     * Post mapping to save a profile picture
     * @param principal Authentication principal storing current user information
     * @param base64FileContent The image content
     * @param fileType The images filetype
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return the addProfilePicture page
     */
    @PostMapping("/addProfilePicture")
    public String addProfilePicture(@AuthenticationPrincipal AuthState principal,
                                    @RequestParam(name="fileContent") String base64FileContent,
                                    @RequestParam(name="fileType") String fileType,
                                    Model model) {

        if (fileType == null) {
            model.addAttribute("errorMessage", "Please select a profile before saving changes");
            return PROFILE_PICTURE_REDIRECT;
        }

        //get userId using the Authentication Principal
        int id = userAccountClientService.getUserId(principal);

        try {
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] decodedByte = decoder.decode(base64FileContent.split(",")[1]);
            userAccountClientService.uploadUserProfilePhoto(decodedByte, id, fileType);
            // Generic attributes that need to be set for the profile page
            User user = userAccountClientService.getUserAccountById(id);
            addProfilePictureToModel(model, user);
            return "redirect:/profile";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e);
        }

        return PROFILE_PICTURE_REDIRECT;
    }

    /**
     * Post mapping to remove the users profile picture
     * @param principal Authentication principal storing current user information
     * @param username The users username
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return The profile page
     */
    @PostMapping("/removeProfilePicture")
    public String removeProfilePicture(@AuthenticationPrincipal AuthState principal,
                                       @RequestParam(name="username") String username,
                                       Model model) {

        //get userId using the Authentication Principle
        int id = Integer.parseInt(principal.getClaimsList().stream()
                .filter(claim -> claim.getType().equals(NAME_ID_CLAIM_TYPE))
                .findFirst()
                .map(ClaimDTO::getValue)
                .orElse("-100"));

        DeleteUserProfilePhotoResponse deleteUserProfilePhotoResponse ;
        deleteUserProfilePhotoResponse = userAccountClientService.deleteUserProfilePhoto(id);

        //Get the new version of user
        User user = userAccountClientService.getUserAccountById(id);
        model.addAttribute("username", username);

        //if remove profile picture was successful return to profile
        if (deleteUserProfilePhotoResponse.getIsSuccess()){
            //generic model attribute that need to be set for the profile page
            addProfilePictureToModel(model, user);
            model.addAttribute("successMessage", "Your profile picture has been successfully been removed");
            return "redirect:/profile";
        } else {
            //if edit user was unsuccessful
            model.addAttribute("deleteMessage", "");
            model.addAttribute("deleteMessage", deleteUserProfilePhotoResponse.getMessage());
            return PROFILE_PICTURE_REDIRECT;
        }
    }

    /**
     * Generic model attribute that need to be set for the profile page
     * @param model is the model that needs to be set
     * @param user is the user that the model belongs to
     */
    private void addProfilePictureToModel(Model model, User user) {
        model.addAttribute("name", user.getFirstName() + " " + user.getLastName());
        Timestamp ts = user.getCreated();
        Instant timeCreated = Instant.ofEpochSecond( ts.getSeconds() , ts.getNanos() );
        LocalDate dateCreated = timeCreated.atZone( ZoneId.systemDefault() ).toLocalDate();
        long months = ChronoUnit.MONTHS.between(dateCreated, LocalDate.now());
        String formattedDate = "Member Since: " + dateCreated + " (" + months + " months)";
        model.addAttribute("date", formattedDate);
    }

}
