package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.PortfolioUser;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.User;
import nz.ac.canterbury.seng302.portfolio.service.PortfolioUserService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
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

    private static final String REGEX_WITH_SPACE = "[a-zA-Z1-9àáâäãåąčćęèéêëėįìíîïłńòóôöõøùúûüųūÿýżźñçšžÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕØÙÚÛÜŲŪŸÝŻŹÑßÇŒÆŠŽ∂ð,. '\\-]";
    private static final String REGEX_WITHOUT_SPACE = "[a-zA-Z1-9àáâäãåąčćęèéêëėįìíîïłńòóôöõøùúûüųūÿýżźñçšžÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕØÙÚÛÜŲŪŸÝŻŹÑßÇŒÆŠŽ∂ð,.'\\-]";
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

    @ModelAttribute("authUserIsTeacher")
    public boolean userIsTeacher(@AuthenticationPrincipal AuthState principal){
        try {
            return userAccountClientService.isTeacher(principal);
        } catch (Exception e) {
            return false;
        }
    }

    @ModelAttribute("authUserIsAdmin")
    public boolean userIsAdmin(@AuthenticationPrincipal AuthState principal){
        try {
            return userAccountClientService.isAdmin(principal);
        } catch (Exception e) {
            return false;
        }
    }

    @ModelAttribute("authUserIsPrivileged")
    public boolean userIsPrivileged(@AuthenticationPrincipal AuthState principal) {
        try {
            return userIsAdmin(principal) || userIsTeacher(principal);
        } catch (Exception e) {
            return false;
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