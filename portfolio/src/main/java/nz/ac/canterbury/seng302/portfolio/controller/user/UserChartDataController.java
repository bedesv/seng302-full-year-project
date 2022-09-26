package nz.ac.canterbury.seng302.portfolio.controller.user;

import nz.ac.canterbury.seng302.portfolio.model.user.User;
import nz.ac.canterbury.seng302.portfolio.service.user.UserAccountClientService;
import nz.ac.canterbury.seng302.portfolio.service.user.UserChartDataService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

@RestController
public class UserChartDataController {

    private static final String TIME_FORMAT = "yyyy-MM-dd";

    @Autowired
    private UserChartDataService userChartDataService;

    @Autowired
    private UserAccountClientService userService;

    private static final Logger PORTFOLIO_LOGGER = LoggerFactory.getLogger("com.portfolio");


    /**
     * Used by the front end to fetch the number of evidence per skill for a given user
     * @param userId The ID of the selected user
     * @param parentProjectId The project ID
     * @param startDateString A string representation of the date that evidence must be on or before to be included in the data.
     * @param endDateString A string representation of the date that evidence must be on or after to be included in the data.
     * @return A map of skill names to the number of times they're used
     */
    @GetMapping("/user-{userId}-skillsData")
    public Map<String, Integer> getSkillsData(@AuthenticationPrincipal AuthState principal,
                                                  @PathVariable int userId,
                                                  @RequestParam int parentProjectId,
                                                  @RequestParam String startDateString,
                                                  @RequestParam String endDateString) {
        User user = userService.getUserAccountByPrincipal(principal);
        if (user.getUsername() == null) {
            return Collections.emptyMap();
        }
        Date startDate;
        Date endDate;
        try {
            startDate = new SimpleDateFormat(TIME_FORMAT).parse(startDateString);
            endDate = new SimpleDateFormat(TIME_FORMAT).parse(endDateString);
        } catch (ParseException e) {
            PORTFOLIO_LOGGER.error(e.getMessage());
            return Collections.emptyMap();
        }
        return userChartDataService.getUserSkillData(userId, parentProjectId, startDate, endDate);
    }
}
