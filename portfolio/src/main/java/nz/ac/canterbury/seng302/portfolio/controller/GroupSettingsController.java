package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class GroupSettingsController {
    private static final String SETTINGS_PAGE = "groupSettings";

    /**
     * Get mapping to fetch group settings page
     * @param principal Authentication principal storing current user information
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return The  group settings html page
     */
    @GetMapping("/groupSettings-{id}")
    public String groups(@AuthenticationPrincipal AuthState principal, Model model, @PathVariable String id){
        return SETTINGS_PAGE;
    }
}
