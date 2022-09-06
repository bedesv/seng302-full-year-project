package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.controller.user.ProfileController;
import nz.ac.canterbury.seng302.portfolio.model.project.Project;
import nz.ac.canterbury.seng302.portfolio.model.user.User;
import nz.ac.canterbury.seng302.portfolio.service.user.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProfileControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    UserAccountClientService userService;

    @MockBean
    GlobalControllerAdvice globalControllerAdvice;


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

    // Check that the profile endpoint works with a valid user
    @Test
    void getStandardProfile_withValidUser_returnProfile() throws Exception {

        AuthState validAuthState = setupSecurity();
        Mockito.when(userService.getUserAccountByPrincipal(validAuthState)).thenReturn(new User(UserResponse.newBuilder().build()));
        Mockito.when(globalControllerAdvice.getCurrentProject(validAuthState)).thenReturn(new Project());
        Mockito.when(globalControllerAdvice.getAllProjects()).thenReturn(List.of(new Project()));
        Mockito.when(globalControllerAdvice.getUser(validAuthState)).thenReturn(new User(UserResponse.newBuilder().setId(1).build()));

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(redirectedUrl(null));
    }

    // Check that the profile endpoint redirects to the requesters profile
    // when viewing a user who does not exist
    @Test
    void getOtherProfile_withInvalidUserReturns_redirectToProfile() throws Exception {

        AuthState validAuthState = setupSecurity();
        Mockito.when(userService.getUserAccountByPrincipal(validAuthState)).thenReturn(new User(UserResponse.newBuilder().build()));
        Mockito.when(userService.getUserAccountById(2)).thenReturn(new User(UserResponse.newBuilder().build()));
        Mockito.when(globalControllerAdvice.getCurrentProject(validAuthState)).thenReturn(new Project());
        Mockito.when(globalControllerAdvice.getAllProjects()).thenReturn(List.of(new Project()));
        Mockito.when(globalControllerAdvice.getUser(validAuthState)).thenReturn(new User(UserResponse.newBuilder().setId(1).build()));

        mockMvc.perform(get("/profile-2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }

    // Check that the profile endpoint works normally
    // when viewing another user
    @Test
    void getOtherProfile_withOtherUser_returnsProfile() throws Exception {

        AuthState validAuthState = setupSecurity();
        Mockito.when(userService.getUserAccountByPrincipal(validAuthState)).thenReturn(new User(UserResponse.newBuilder().setId(1).build()));
        Mockito.when(userService.getUserAccountById(2)).thenReturn(new User(UserResponse.newBuilder().setId(2).setUsername("test").build()));
        Mockito.when(globalControllerAdvice.getCurrentProject(validAuthState)).thenReturn(new Project());
        Mockito.when(globalControllerAdvice.getAllProjects()).thenReturn(List.of(new Project()));
        Mockito.when(globalControllerAdvice.getUser(validAuthState)).thenReturn(new User(UserResponse.newBuilder().setId(1).build()));

        mockMvc.perform(get("/profile-2"))
                .andExpect(status().isOk())
                .andExpect(redirectedUrl(null));
    }

    // Check that the profile endpoint redirects to the requesters profile
    // when viewing your own profile
    @Test
    void getOtherProfile_withSameUser_returnsRedirectToProfile() throws Exception {

        AuthState validAuthState = setupSecurity();
        Mockito.when(userService.getUserAccountByPrincipal(validAuthState)).thenReturn(new User(UserResponse.newBuilder().setId(1).build()));
        Mockito.when(userService.getUserAccountById(1)).thenReturn(new User(UserResponse.newBuilder().setId(1).setUsername("test").build()));
        Mockito.when(globalControllerAdvice.getCurrentProject(validAuthState)).thenReturn(new Project());
        Mockito.when(globalControllerAdvice.getAllProjects()).thenReturn(List.of(new Project()));
        Mockito.when(globalControllerAdvice.getUser(validAuthState)).thenReturn(new User(UserResponse.newBuilder().setId(1).build()));

        mockMvc.perform(get("/profile-1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }

}
