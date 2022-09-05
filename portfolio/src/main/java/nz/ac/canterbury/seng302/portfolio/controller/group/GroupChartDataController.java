package nz.ac.canterbury.seng302.portfolio.controller.group;

import nz.ac.canterbury.seng302.portfolio.model.group.Group;
import nz.ac.canterbury.seng302.portfolio.model.user.User;
import nz.ac.canterbury.seng302.portfolio.service.group.GroupChartDataService;
import nz.ac.canterbury.seng302.portfolio.service.group.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.user.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;


@RestController
public class GroupChartDataController {

    private static final String TIME_FORMAT = "yyyy-MM-dd";

    @Autowired
    private GroupsClientService groupsClientService;

    @Autowired
    private GroupChartDataService groupChartDataService;

    @Autowired
    private UserAccountClientService userService;

    private static final Logger PORTFOLIO_LOGGER = LoggerFactory.getLogger("com.portfolio");


    /**
     * Used by the front end to fetch the number of commits for each category
     * @param groupId The id of the group that data is requested for
     * @return A map of category names to the number of times they're used
     */
    @GetMapping("/group-{groupId}-categoriesData")
    public Map<String, Integer> getCategoriesData(@PathVariable int groupId) {
        Group group = new Group(groupsClientService.getGroupDetailsById(groupId));
        return groupChartDataService.getGroupCategoryInfo(group);
    }

    /**
     * Used by the front end to fetch the number of evidence per skill
     * @param endDateString A string representation of the date that evidence must be on or before to be included in the data.
     * @param startDateString A string representation of the date that evidence must be on or after to be included in the data.
     * @param groupId The id of the group that data is requested for
     * @return A map of skill names to the number of times they're used
     */
    @GetMapping("/group-{groupId}-skillsData")
    public Map<String, Integer> getSkillsData(@AuthenticationPrincipal AuthState principal,
                                                            @PathVariable int groupId,
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
        Group group = new Group(groupsClientService.getGroupDetailsById(groupId));
        return groupChartDataService.getGroupSkillData(group, startDate, endDate);
    }

}
