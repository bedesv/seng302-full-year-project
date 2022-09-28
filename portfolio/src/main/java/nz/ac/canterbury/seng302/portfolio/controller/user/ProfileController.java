package nz.ac.canterbury.seng302.portfolio.controller.user;

import nz.ac.canterbury.seng302.portfolio.model.group.Group;
import nz.ac.canterbury.seng302.portfolio.model.user.User;
import nz.ac.canterbury.seng302.portfolio.service.group.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.user.PortfolioUserService;
import nz.ac.canterbury.seng302.portfolio.service.user.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The controller for handling backend of the profile page
 */
@Controller
public class ProfileController {

    @Autowired
    private UserAccountClientService userService;

    @Autowired
    private GroupsClientService groupsClientService;

    @Autowired
    private PortfolioUserService portfolioUserService;
    private static final String PORTFOLIO_SELECTED = "portfolioSelected";

    /**
     * Display the user's profile page.
     * @param principal Authentication state of client
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return The string "profile"
     */
    @GetMapping("/profile")
    public String profile(
            @AuthenticationPrincipal AuthState principal,
            @RequestParam(PORTFOLIO_SELECTED) Optional<Boolean> portfolioSelected,
            Model model
    ) {
        User user = userService.getUserAccountByPrincipal(principal);
        if (portfolioSelected.isPresent()) {
            model.addAttribute(PORTFOLIO_SELECTED, portfolioSelected.get());
        } else {
            model.addAttribute(PORTFOLIO_SELECTED, false);
        }
        model.addAttribute("portfolioLinks", true);
        int projectId = portfolioUserService.getCurrentProject(user.getId()).getId();
        List<Group> groups = groupsClientService.getAllGroupsUserIn(projectId, user.getId());

        model.addAttribute("pageUser", user);
        model.addAttribute("groups", groups);
        model.addAttribute("owner", true);
        return "templatesUser/user";
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
            @RequestParam(PORTFOLIO_SELECTED) Optional<Boolean> portfolioSelected,
            @PathVariable("userId") int userId,
            Model model
    ) {
        User user = userService.getUserAccountByPrincipal(principal);
        User pageUser = userService.getUserAccountById(userId);
        int projectId = portfolioUserService.getCurrentProject(pageUser.getId()).getId();
        List<Group> groups = groupsClientService.getAllGroupsUserIn(projectId, pageUser.getId());
        model.addAttribute("pageUser", pageUser);
        if (portfolioSelected.isPresent()) {
            model.addAttribute(PORTFOLIO_SELECTED, portfolioSelected.get());
        } else {
            model.addAttribute(PORTFOLIO_SELECTED, false);
        }
        model.addAttribute("portfolioLinks", true);
        model.addAttribute("groups", groups);
        if (Objects.equals(pageUser.getUsername(), "") || user.getId() == pageUser.getId()) {
            return "redirect:/profile";
        } else {
            model.addAttribute("owner", false);
            return "templatesUser/user";
        }
    }
}

