package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.user.PortfolioUser;
import nz.ac.canterbury.seng302.portfolio.model.project.Project;
import nz.ac.canterbury.seng302.portfolio.model.user.User;
import nz.ac.canterbury.seng302.portfolio.service.user.PortfolioUserService;
import nz.ac.canterbury.seng302.portfolio.service.project.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.user.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice extends ResponseEntityExceptionHandler {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private PortfolioUserService portfolioUserService;

    @Autowired
    private UserAccountClientService userAccountClientService;

    private static final List<String> CATEGORIES_LIST = List.of("Quantitative", "Qualitative", "Service");

    @ModelAttribute("allProjects")
    public List<Project> getAllProjects(){
        return projectService.getAllProjects();
    }

    @ModelAttribute("categoryList")
    public List<String> getAllCategories(){
        return CATEGORIES_LIST;
    }

    @ModelAttribute("user")
    public User getUser(@AuthenticationPrincipal AuthState principal) {
        try {
            int userId = Integer.parseInt(principal.getClaimsList().stream()
                    .filter(claim -> claim.getType().equals("nameid"))
                    .findFirst()
                    .map(ClaimDTO::getValue)
                    .orElse("-100"));
            return userAccountClientService.getUserAccountById(userId);
        } catch (Exception e) {
            return null;
        }
    }

    @ModelAttribute("currentProject")
    public Project getCurrentProject(@AuthenticationPrincipal AuthState principal) {
        int id;
        try {
            id = Integer.parseInt(principal.getClaimsList().stream()
                    .filter(claim -> claim.getType().equals("nameid"))
                    .findFirst()
                    .map(ClaimDTO::getValue)
                    .orElse("-100"));

            PortfolioUser user = portfolioUserService.getUserById(id);
            return portfolioUserService.getCurrentProject(user.getUserId());
        } catch (Exception e) {
            if (projectService.getAllProjects().isEmpty()){
                return new Project();
            } else {
                return projectService.getAllProjects().get(0);
            }
        }
    }

    /**
     * This attribute will be used in th:pattern to ensure that fields are not blank
     * Then additional validation to be carried out in the service
     * @return regex that will reject blank strings.
     */
    @ModelAttribute("isNotBlankPattern")
    public String getIsNotBlankPattern(){
        return "(.|\\s)*\\S(.|\\s)*";
    }
}