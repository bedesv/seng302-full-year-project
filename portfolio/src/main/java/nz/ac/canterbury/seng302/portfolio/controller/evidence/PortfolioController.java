package nz.ac.canterbury.seng302.portfolio.controller.evidence;

import nz.ac.canterbury.seng302.portfolio.model.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.model.evidence.PortfolioEvidence;
import nz.ac.canterbury.seng302.portfolio.model.user.User;
import nz.ac.canterbury.seng302.portfolio.model.evidence.WebLink;
import nz.ac.canterbury.seng302.portfolio.service.evidence.EvidenceService;
import nz.ac.canterbury.seng302.portfolio.service.user.PortfolioUserService;
import nz.ac.canterbury.seng302.portfolio.service.user.UserAccountClientService;
import nz.ac.canterbury.seng302.portfolio.util.ValidationUtil;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

/**
 * The controller for handling backend of the portfolio page
 */
@Controller
public class PortfolioController {

    @Autowired
    private UserAccountClientService userService;

    @Autowired
    private EvidenceService evidenceService;

    @Autowired
    private PortfolioUserService portfolioUserService;

    private static final String PORTFOLIO_REDIRECT = "redirect:/portfolio";
    private static final String OWNER = "owner";
    private static final int MAX_WEBLINKS_PER_EVIDENCE = 5;

    /**
     * Display the user's portfolio page.
     * @param principal Authentication state of client
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return The portfolio page.
     */
    @GetMapping("/portfolio")
    public String portfolio(
            @AuthenticationPrincipal AuthState principal,
            Model model
    ) {
        User user = userService.getUserAccountByPrincipal(principal);
        model.addAttribute("pageUser", user);
        model.addAttribute(OWNER, true);

        int userId = user.getId();
        int projectId = portfolioUserService.getUserById(userId).getCurrentProject();
        List<PortfolioEvidence> evidenceList = evidenceService.getEvidenceForPortfolio(userId, projectId);

        model.addAttribute("evidenceList", evidenceList);
        model.addAttribute("skillsList", evidenceService.getSkillsFromPortfolioEvidence(evidenceList));
        model.addAttribute("maxWeblinks", MAX_WEBLINKS_PER_EVIDENCE);
        return "templatesEvidence/portfolio";
    }

    /**
     * Display a user's portfolio page.
     * If the user does not exist redirects to the requesters profile page.
     * Users who try to view their own page are taken to a page with edit permissions.
     * @param principal Authentication state of client
     * @param userId The ID of the user whose portfolio we are viewing
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return The portfolio page, or a redirect to profile if the user does not exist.
     */
    @GetMapping("/portfolio-{userId}")
    public String viewPortfolio(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("userId") int userId,
            Model model
    ) {
        User pageUser = userService.getUserAccountByPrincipal(principal);
        User user = userService.getUserAccountById(userId);
        model.addAttribute("pageUser", pageUser);

        int projectId = portfolioUserService.getUserById(pageUser.getId()).getCurrentProject();
        List<PortfolioEvidence> evidenceList = evidenceService.getEvidenceForPortfolio(user.getId(), projectId);

        model.addAttribute("evidenceList", evidenceList);

        // Add all of the skills that the user has to the page
        List<PortfolioEvidence> allUsersEvidenceList = evidenceService.getEvidenceForPortfolio(user.getId(), projectId);
        model.addAttribute("skillsList", evidenceService.getSkillsFromPortfolioEvidence(allUsersEvidenceList));
        model.addAttribute("maxWeblinks", MAX_WEBLINKS_PER_EVIDENCE);
        if (Objects.equals(user.getUsername(), "")) {
            return "redirect:/profile";
        } else if (user.getId() == pageUser.getId()) {
            return PORTFOLIO_REDIRECT; // Take user to their own portfolio if they try to view it
        } else {
            model.addAttribute(OWNER, false);
            return "templatesEvidence/portfolio";
        }
    }

    /**
     * Save or edit one web link. Redirects to portfolio page with no message if evidence does not exist.
     * If correctly saved, sends web link html element to update on page.
     * @param evidenceId id of the evidence to add the link to
     * @param webLink The link string to be added to evidence of id=evidenceId
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return web link html element.
     */
    @PostMapping("/addWebLink-{evidenceId}")
    public String addWebLink(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable(name="evidenceId") String evidenceId,
            @RequestParam(name="webLink") String webLink,
            @RequestParam(name="webLinkName") String webLinkName,
            @RequestParam(name="webLinkIndex") String webLinkIndex,
            Model model
    ) {
        User user = userService.getUserAccountByPrincipal(principal);
        int id = Integer.parseInt(evidenceId);
        int index = Integer.parseInt(webLinkIndex);
        Evidence evidence = evidenceService.getEvidenceById(id);
        if (user.getId() != evidence.getOwnerId()) {
            model.addAttribute(OWNER, false);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cannot modify other users' evidence");
        } else {
            model.addAttribute(OWNER, true);
        }

        webLink = ValidationUtil.stripTitle(webLink);
        webLinkName = ValidationUtil.stripTitle(webLinkName);

        if (evidence.getNumberWeblinks() >= MAX_WEBLINKS_PER_EVIDENCE) {
            model.addAttribute("saveError", "Cannot add more than " + MAX_WEBLINKS_PER_EVIDENCE + " weblinks");
        } else {
            try {
                evidenceService.validateWebLink(webLink);
                WebLink webLink1;
                if (webLinkName.isEmpty()) {
                    webLink1 = new WebLink(webLink);
                } else {
                    webLink1 = new WebLink(webLink, webLinkName);
                }
                if (index > -1) {
                    evidenceService.modifyWebLink(id, webLink1, index);
                } else {
                    evidenceService.saveWebLink(id, webLink1);
                }

            } catch (Exception e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Incorrect Web Link Format", e);
            }
        }
        model.addAttribute("webLinks", evidence.getWebLinks());
        model.addAttribute("evidenceId", evidence.getId());
        return "fragments/webLink";
    }

    /**
     * Gets the web links of evidence with evidenceId in html element.
     * @param evidenceId Id of evidence to find weblinks for.
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return web link html element.
     */
    @GetMapping("/getWebLinks-{evidenceId}")
    public String getWebLinks(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable(name="evidenceId") String evidenceId,
            Model model
    ) {
        User user = userService.getUserAccountByPrincipal(principal);
        int id = Integer.parseInt(evidenceId);
        Evidence evidence = evidenceService.getEvidenceById(id);
        model.addAttribute(OWNER, user.getId() == evidence.getOwnerId());
        model.addAttribute("webLinks", evidence.getWebLinks());
        model.addAttribute("evidenceId", evidence.getId());
        return "fragments/webLink";
    }

    /**
     * Gets the web links of evidence with evidenceId in html element.
     * @param evidenceId Id of evidence to find weblinks for.
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return web link html element.
     */
    @GetMapping("/evidence-{evidenceId}-highfive")
    public String getEvidenceHighFive(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable(name="evidenceId") String evidenceId,
            Model model
    ) {
        User user = userService.getUserAccountByPrincipal(principal);
        int id = Integer.parseInt(evidenceId);
        Evidence evidence = evidenceService.getEvidenceById(id);
        model.addAttribute(OWNER, user.getId() == evidence.getOwnerId());
        PortfolioEvidence portfolioEvidence = evidenceService.convertEvidenceForPortfolio(List.of(evidence)).get(0);
        model.addAttribute("evidence", portfolioEvidence);
        model.addAttribute("pageUser", user);
        return "fragments/highfive";
    }

    /**
     * Calls the toggle high five method where a user is added to an evidence's high-fives
     * @param evidenceIdString ID of evidence being high-fived
     */
    @PostMapping("/evidence-{evidenceId}-highfive")
    @ResponseStatus(value = HttpStatus.OK)
    public void toggleHighFive(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable(name="evidenceId") String evidenceIdString
    ) {
        int userId = userService.getUserAccountByPrincipal(principal).getId();
        int evidenceId = Integer.parseInt(evidenceIdString);
        evidenceService.toggleHighFive(evidenceId, userId);
    }

}

