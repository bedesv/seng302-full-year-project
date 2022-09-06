package nz.ac.canterbury.seng302.portfolio.controller.user;

import nz.ac.canterbury.seng302.portfolio.model.user.User;
import nz.ac.canterbury.seng302.portfolio.service.user.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Objects;

/**
 * The controller for handling backend of the profile page
 */
@Controller
public class ProfileController {

    @Autowired
    private UserAccountClientService userService;

    /**
     * Display the user's profile page.
     * @param principal Authentication state of client
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return The string "profile"
     */
    @GetMapping("/profile")
    public String profile(
            @AuthenticationPrincipal AuthState principal,
            Model model
    ) {
        User user = userService.getUserAccountByPrincipal(principal);
        model.addAttribute("pageUser", user);
        model.addAttribute("owner", true);
        return "templatesUser/profile";
    }

    /**
     * Display another user's profile page. If the user does not exist redirect to the requesters profile page.
     * @param principal Authentication state of client
     * @param userId The ID of the user whose profile we are viewing
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return The string "profile"
     */
    @GetMapping("/profile-{userId}")
    public String viewProfile(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("userId") int userId,
            Model model
    ) {
        User user = userService.getUserAccountByPrincipal(principal);
        User pageUser = userService.getUserAccountById(userId);
        model.addAttribute("pageUser", pageUser);
        if (Objects.equals(pageUser.getUsername(), "") || user.getId() == pageUser.getId()) {
            return "redirect:/profile";
        } else {
            model.addAttribute("owner", false);
            return "templatesUser/profile";
        }
    }
}

