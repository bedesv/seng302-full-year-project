package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.controller.evidence.AddEvidenceController;
import nz.ac.canterbury.seng302.portfolio.model.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.model.group.GroupListResponse;
import nz.ac.canterbury.seng302.portfolio.model.user.PortfolioUser;
import nz.ac.canterbury.seng302.portfolio.model.project.Project;
import nz.ac.canterbury.seng302.portfolio.model.user.User;
import nz.ac.canterbury.seng302.portfolio.service.evidence.EvidenceService;
import nz.ac.canterbury.seng302.portfolio.service.group.GitlabConnectionService;
import nz.ac.canterbury.seng302.portfolio.service.group.GroupRepositorySettingsService;
import nz.ac.canterbury.seng302.portfolio.service.group.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.project.SprintService;
import nz.ac.canterbury.seng302.portfolio.service.user.PortfolioUserService;
import nz.ac.canterbury.seng302.portfolio.service.project.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.user.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import nz.ac.canterbury.seng302.shared.identityprovider.PaginatedGroupsResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AddEvidenceController.class)
@AutoConfigureMockMvc(addFilters = false)
class AddEvidenceControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    UserAccountClientService userService;

    @MockBean
    ProjectService projectService;

    @MockBean
    PortfolioUserService portfolioUserService;

    @MockBean
    EvidenceService evidenceService;

    @MockBean
    GroupRepositorySettingsService groupRepositorySettingsService;

    @MockBean
    GroupsClientService groupClientService;

    @MockBean
    GlobalControllerAdvice globalControllerAdvice;

    @MockBean
    GitlabConnectionService gitlabConnectionService;

    @MockBean
    SprintService sprintService;

    /**
     * Helper function to create a valid AuthState given an ID
     * Credit to teaching team for this function
     * @param id - The ID of the user specified by this AuthState
     * @return the valid AuthState
     */
    private AuthState createValidAuthStateWithId(String id) {
        return AuthState.newBuilder()
                .setIsAuthenticated(true)
                .setNameClaimType("name")
                .setRoleClaimType("role")
                .addClaims(ClaimDTO.newBuilder().setType("role").setValue("STUDENT").build())
                .addClaims(ClaimDTO.newBuilder().setType("nameid").setValue(id).build())
                .build();
    }

    /**
     * Helper function which sets up the security of the test for simple uses.
     * @return a valid AuthState with ID 1
     */
    private AuthState setupSecurity() {
        AuthState validAuthState = createValidAuthStateWithId("1");
        SecurityContext mockedSecurityContext = Mockito.mock((SecurityContext.class));
        Mockito.when(mockedSecurityContext.getAuthentication()).thenReturn(new PreAuthenticatedAuthenticationToken(validAuthState,""));
        SecurityContextHolder.setContext(mockedSecurityContext);
        return validAuthState;
    }

    // Makes it so no groups exist so that they do not interfere with the tests
    @BeforeEach
    void setupGroups() {
        Mockito.when(groupClientService.getAllGroups()).thenReturn(new GroupListResponse(PaginatedGroupsResponse.newBuilder().build()));
    }

    // Check that the portfolio endpoint works with a valid user
    @Test
    void whenGetAddEvidencePage_testReturnsAddEvidence() throws Exception {
        AuthState validAuthState = setupSecurity();
        Mockito.when(userService.getUserAccountByPrincipal(validAuthState)).thenReturn(new User(UserResponse.newBuilder().build()));
        PortfolioUser portfolioUser = new PortfolioUser(1, "name", true);
        portfolioUser.setCurrentProject(1);
        Mockito.when(portfolioUserService.getUserById(any(Integer.class))).thenReturn(portfolioUser);
        Mockito.when(projectService.getProjectById(any(Integer.class))).thenReturn(new Project());
        Mockito.when(globalControllerAdvice.getCurrentProject(validAuthState)).thenReturn(new Project());
        Mockito.when(globalControllerAdvice.getAllProjects()).thenReturn(List.of(new Project()));
        Mockito.when(globalControllerAdvice.getUser(validAuthState)).thenReturn(new User(UserResponse.newBuilder().setId(1).build()));

        mockMvc.perform(get("/editEvidence--1"))
                .andExpect(status().isOk())
                .andExpect(redirectedUrl(null));
    }

    // Check that saving evidence properly redirects to the portfolio page.
    @Test
    void whenSaveEvidenceWithGoodData_testReturnsPortfolio() throws Exception {
        AuthState validAuthState = setupSecurity();
        Mockito.when(userService.getUserAccountByPrincipal(validAuthState)).thenReturn(new User(UserResponse.newBuilder().setId(1).build()));
        Mockito.when(userService.getUserId(validAuthState)).thenReturn(1);
        Mockito.when(portfolioUserService.getUserById(1)).thenReturn(new PortfolioUser(1, "name", true));
        Mockito.when(globalControllerAdvice.getCurrentProject(validAuthState)).thenReturn(new Project());
        Mockito.when(globalControllerAdvice.getAllProjects()).thenReturn(List.of(new Project()));
        Mockito.when(projectService.getProjectById(any(Integer.class))).thenReturn(new Project());
        Mockito.when(globalControllerAdvice.getUser(validAuthState)).thenReturn(new User(UserResponse.newBuilder().setId(1).build()));

        mockMvc.perform(post("/editEvidence--1")
                        .param("evidenceTitle", "test title")
                        .param("evidenceDescription", "test description")
                        .param("evidenceDate", "2002-02-16")
                        .param("evidenceSkills", "")
                        .param("isQuantitative", "")
                        .param("isQualitative", "")
                        .param("isService", "")
                        .param("evidenceCommits", "")
                        .param("evidenceUsers", "")
                        .param("evidenceSkills", "")
                        .param("skillsToChange", "")
                        .param("evidenceWebLinks", "")
                        .param("evidenceWebLinkNames", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inPortfolio"));
    }

    // Check that saving a piece of evidence properly redirects to the portfolio page.
    @Test
    void whenSaveEvidenceWithBadData_testReturnsAddEvidence() throws Exception {
        AuthState validAuthState = setupSecurity();
        Mockito.when(userService.getUserAccountByPrincipal(validAuthState)).thenReturn(new User(UserResponse.newBuilder().build()));
        Mockito.when(userService.getUserId(validAuthState)).thenReturn(1);
        Mockito.when(portfolioUserService.getUserById(any(Integer.class))).thenReturn(new PortfolioUser(1, "name", true));
        Mockito.doThrow(IllegalArgumentException.class).when(evidenceService).saveEvidence(any());
        Mockito.when(globalControllerAdvice.getCurrentProject(validAuthState)).thenReturn(new Project());
        Mockito.when(globalControllerAdvice.getAllProjects()).thenReturn(List.of(new Project()));
        Mockito.when(globalControllerAdvice.getUser(validAuthState)).thenReturn(new User(UserResponse.newBuilder().setId(1).build()));
        Mockito.when(projectService.getProjectById(any(Integer.class))).thenReturn(new Project());

        mockMvc.perform(post("/editEvidence--1")
                        .param("evidenceTitle", "test title")
                        .param("evidenceDescription", "test description")
                        .param("evidenceDate", "2002-02-16")
                        .param("evidenceSkills", "")
                        .param("isQuantitative", "")
                        .param("isQualitative", "")
                        .param("isService", "")
                        .param("evidenceCommits", "")
                        .param("evidenceUsers", "")
                        .param("skillsToChange", ""))
                .andExpect(status().isOk())
                .andExpect(redirectedUrl(null));
    }

    @Test
    void whenSaveEvidenceWithBadDate_testReturnsAddEvidence() throws Exception {
        AuthState validAuthState = setupSecurity();
        Mockito.when(userService.getUserAccountByPrincipal(validAuthState)).thenReturn(new User(UserResponse.newBuilder().build()));
        Mockito.when(userService.getUserId(validAuthState)).thenReturn(1);
        Mockito.when(portfolioUserService.getUserById(any(Integer.class))).thenReturn(new PortfolioUser(1, "name", true));
        Mockito.when(globalControllerAdvice.getCurrentProject(validAuthState)).thenReturn(new Project());
        Mockito.when(globalControllerAdvice.getAllProjects()).thenReturn(List.of(new Project()));
        Mockito.when(globalControllerAdvice.getUser(validAuthState)).thenReturn(new User(UserResponse.newBuilder().setId(1).build()));
        Mockito.when(projectService.getProjectById(any(Integer.class))).thenReturn(new Project());

        mockMvc.perform(post("/editEvidence--1")
                        .param("evidenceTitle", "test title")
                        .param("evidenceDescription", "test description")
                        .param("evidenceDate", "bad date")
                        .param("evidenceSkills", "")
                        .param("isQuantitative", "")
                        .param("isQualitative", "")
                        .param("isService", "")
                        .param("evidenceCommits", "")
                        .param("evidenceUsers", "")
                        .param("skillsToChange", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inPortfolio"));
    }

    // Check that sending malformed commit data results in a redirect to the portfolio
    @Test
    void whenSaveEvidenceWithBadCommits_testReturnsAddEvidence() throws Exception {
        AuthState validAuthState = setupSecurity();
        Mockito.when(userService.getUserAccountByPrincipal(validAuthState)).thenReturn(new User(UserResponse.newBuilder().build()));
        Mockito.when(userService.getUserId(validAuthState)).thenReturn(1);
        Mockito.when(portfolioUserService.getUserById(any(Integer.class))).thenReturn(new PortfolioUser(1, "name", true));
        Mockito.when(globalControllerAdvice.getCurrentProject(validAuthState)).thenReturn(new Project());
        Mockito.when(globalControllerAdvice.getAllProjects()).thenReturn(List.of(new Project()));
        Mockito.when(globalControllerAdvice.getUser(validAuthState)).thenReturn(new User(UserResponse.newBuilder().setId(1).build()));
        Mockito.when(projectService.getProjectById(any(Integer.class))).thenReturn(new Project());

        mockMvc.perform(post("/editEvidence--1")
                        .param("evidenceTitle", "test title")
                        .param("evidenceDescription", "test description")
                        .param("evidenceDate", "2021-12-12")
                        .param("evidenceSkills", "")
                        .param("isQuantitative", "")
                        .param("isQualitative", "")
                        .param("isService", "")
                        .param("evidenceCommits", "bad commit data")
                        .param("evidenceUsers", "")
                        .param("skillsToChange", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inPortfolio"));
    }

    // Check that trying to edit evidence with a mangled evidence id fails.
    @Test
    void whenEditEvidenceWithBadEvidence_testReturnsPortfolio() throws Exception {
        AuthState validAuthState = setupSecurity();
        Mockito.when(userService.getUserAccountByPrincipal(validAuthState)).thenReturn(new User(UserResponse.newBuilder().setId(1).build()));
        Mockito.when(userService.getUserId(validAuthState)).thenReturn(1);
        PortfolioUser user = new PortfolioUser(1, "name", true);
        user.setCurrentProject(1);
        Mockito.when(portfolioUserService.getUserById(1)).thenReturn(user);
        Mockito.when(globalControllerAdvice.getCurrentProject(validAuthState)).thenReturn(new Project());
        Mockito.when(globalControllerAdvice.getAllProjects()).thenReturn(List.of(new Project()));
        Mockito.when(projectService.getProjectById(any(Integer.class))).thenReturn(new Project());

        mockMvc.perform(get("/editEvidence-bad"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inPortfolio"));
    }

    // Check that trying to edit evidence with a nonexistent evidence id fails.
    @Test
    void whenEditEvidenceWithNonexistentEvidence_testReturnsPortfolio() throws Exception {
        AuthState validAuthState = setupSecurity();
        Mockito.when(userService.getUserAccountByPrincipal(validAuthState)).thenReturn(new User(UserResponse.newBuilder().setId(1).build()));
        Mockito.when(userService.getUserId(validAuthState)).thenReturn(1);
        PortfolioUser portfolioUser = new PortfolioUser(1, "name", true);
        portfolioUser.setCurrentProject(1);
        Mockito.when(portfolioUserService.getUserById(1)).thenReturn(portfolioUser);
        Mockito.when(globalControllerAdvice.getCurrentProject(validAuthState)).thenReturn(new Project());
        Mockito.when(globalControllerAdvice.getAllProjects()).thenReturn(List.of(new Project()));
        User user = new User(UserResponse.newBuilder().build());
        Mockito.when(globalControllerAdvice.getUser(validAuthState)).thenReturn(user);
        Mockito.when(projectService.getProjectById(any(Integer.class))).thenReturn(new Project());
        // This line is what makes the evidence not exist
        Mockito.when(evidenceService.getEvidenceById(1)).thenThrow(new NoSuchElementException());

        mockMvc.perform(get("/editEvidence-1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inPortfolio"));
    }

    // Check that trying to edit evidence with a real evidence but the wrong user fails.
    @Test
    void whenEditEvidenceWithRealEvidenceButWrongUser_testReturnsPortfolio() throws Exception {
        AuthState validAuthState = setupSecurity();
        Mockito.when(userService.getUserAccountByPrincipal(validAuthState)).thenReturn(new User(UserResponse.newBuilder().setId(1).build()));
        Mockito.when(userService.getUserId(validAuthState)).thenReturn(1);
        PortfolioUser user = new PortfolioUser(1, "name", true);
        user.setCurrentProject(1);
        Mockito.when(portfolioUserService.getUserById(1)).thenReturn(user);
        Mockito.when(globalControllerAdvice.getCurrentProject(validAuthState)).thenReturn(new Project());
        Mockito.when(globalControllerAdvice.getAllProjects()).thenReturn(List.of(new Project()));
        Mockito.when(projectService.getProjectById(any(Integer.class))).thenReturn(new Project());
        // This line is what makes the evidence exist
        Mockito.when(evidenceService.getEvidenceById(1)).thenReturn(new Evidence(2, 1, "test", "test", new Date()));

        mockMvc.perform(get("/editEvidence-1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inPortfolio"));
    }

    // Check that trying to edit evidence with a real evidence succeeds.
    @Test
    void whenEditEvidenceWithRealEvidence_testReturnsEditEvidence() throws Exception {
        AuthState validAuthState = setupSecurity();
        Mockito.when(userService.getUserAccountByPrincipal(validAuthState)).thenReturn(new User(UserResponse.newBuilder().setId(1).build()));
        Mockito.when(userService.getUserId(validAuthState)).thenReturn(1);
        PortfolioUser portfolioUser = new PortfolioUser(1, "name", true);
        portfolioUser.setCurrentProject(1);
        Mockito.when(portfolioUserService.getUserById(1)).thenReturn(portfolioUser);
        User user = new User(UserResponse.newBuilder().build());
        Mockito.when(globalControllerAdvice.getUser(validAuthState)).thenReturn(user);
        Mockito.when(globalControllerAdvice.getCurrentProject(validAuthState)).thenReturn(new Project());
        Mockito.when(globalControllerAdvice.getAllProjects()).thenReturn(List.of(new Project()));
        Mockito.when(projectService.getProjectById(any(Integer.class))).thenReturn(new Project());
        // This line is what makes the evidence exist
        Mockito.when(evidenceService.getEvidenceById(1)).thenReturn(new Evidence(1, 1, "test", "test", new Date()));

        mockMvc.perform(get("/editEvidence-1"))
                .andExpect(status().isOk())
                .andExpect(redirectedUrl(null));
    }

    // Check that trying to edit evidence with a project that does not exist fails.
    @Test
    void whenAddEvidenceWithNoProject_testReturnsPortfolio() throws Exception {
        AuthState validAuthState = setupSecurity();
        Mockito.when(userService.getUserAccountByPrincipal(validAuthState)).thenReturn(new User(UserResponse.newBuilder().setId(1).build()));
        Mockito.when(userService.getUserId(validAuthState)).thenReturn(1);
        Mockito.when(portfolioUserService.getUserById(1)).thenReturn(new PortfolioUser(1, "name", true));
        Mockito.when(globalControllerAdvice.getCurrentProject(validAuthState)).thenReturn(new Project());
        Mockito.when(globalControllerAdvice.getAllProjects()).thenReturn(List.of(new Project()));

        mockMvc.perform(get("/editEvidence-1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inPortfolio"));
    }

}
