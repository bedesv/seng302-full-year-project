package nz.ac.canterbury.seng302.portfolio.controller;

import io.grpc.StatusRuntimeException;
import nz.ac.canterbury.seng302.portfolio.authentication.CookieUtil;
import nz.ac.canterbury.seng302.portfolio.model.User;
import nz.ac.canterbury.seng302.portfolio.service.AuthenticateClientService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Objects;

/**
 * Class which handles the backend of the login page
 */
@Controller
public class LoginController {

    private static final String LOGIN = "login";

    @Autowired
    private AuthenticateClientService authenticateClientService;

    @Autowired
    private UserAccountClientService userAccountClientService;

    private static final String COOKIE_NAME = "lens-session-token";

    /**
     * Gets the mapping to the login page html and renders it
     * @param response Login response
     * @return the mapping to the login html page.
     */
    @GetMapping("/login")
    public String login(@AuthenticationPrincipal AuthState principal,
                        HttpServletResponse response) {
        if (principal == null) {
            return LOGIN;
        }
        User user = userAccountClientService.getUserAccountByPrincipal(principal);
        if (Objects.equals(user.getUsername(), "")) {
            return LOGIN;
        } else {
            return "redirect:/profile";
        }
    }

    /**
     * Gets the mapping to the login page html and renders it
     * @param response Login response
     * @return the mapping to the login html page.
     */
    @GetMapping("/")
    public String home(HttpServletResponse response) {
        return "redirect:/" + LOGIN;
    }

    /**
     * Logs the user out, then gets the mapping to the login page html and renders it
     * @param response Login response
     * @return the mapping to the login html page.
     */
    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        CookieUtil.clear(
                response,
                COOKIE_NAME
        );
        return "redirect:/" + LOGIN;
    }


    /**
     * Attempts to authenticate with the Identity Provider via gRPC.
     *
     * This process works in a few stages:
     *  1.  We send the username and password to the IdP
     *  2.  We check the response, and if it is successful we add a cookie to the HTTP response so that
     *      the client's browser will store it to be used for future authentication with this service.
     *  3.  We return the thymeleaf login template with the 'message' given by the identity provider,
     *      this message will be something along the lines of "Logged in successfully!",
     *      "Bad username or password", etc.
     *
     * @param request HTTP request sent to this endpoint
     * @param response HTTP response that will be returned by this endpoint
     * @param username Username of account to log in to IdP with
     * @param password Password associated with username
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return Message generated by IdP about authenticate attempt
     */
    @PostMapping("/login")
    public String login(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(name="username") String username,
            @RequestParam(name="password") String password,
            Model model
    ) {
        //Check for emojis early, prevents grpc error
        if (!userAccountClientService.validAttribute(model, "username", username)){
            return LOGIN;
        }

        AuthenticateResponse loginReply;
        try {
            loginReply = authenticateClientService.authenticate(username.toLowerCase(Locale.ROOT), password);
        } catch (StatusRuntimeException e){
            model.addAttribute("loginMessage", "Error connecting to Identity Provider...");
            return LOGIN;
        }
        if (loginReply.getSuccess()) {
            var domain = request.getHeader("host");
            CookieUtil.create(
                response,
                    COOKIE_NAME,
                    loginReply.getToken(),
                true,
                5 * 60 * 60, // Expires in 5 hours
                domain.startsWith("localhost") ? null : domain
            );

            return "redirect:/profile";
        } else {
            model.addAttribute("loginMessage", loginReply.getMessage());
            return LOGIN;
        }
    }


}
