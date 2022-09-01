package nz.ac.canterbury.seng302.portfolio.controller.group;

import nz.ac.canterbury.seng302.portfolio.model.group.Group;
import nz.ac.canterbury.seng302.portfolio.service.group.GroupChartDataService;
import nz.ac.canterbury.seng302.portfolio.service.group.GroupsClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;


@Controller
public class GroupChartDataController {

    private static final String TIME_FORMAT = "yyyy-MM-dd";
    @Autowired
    private GroupsClientService groupsClientService;
    @Autowired
    private GroupChartDataService groupChartDataService;

    private static final Logger PORTFOLIO_LOGGER = LoggerFactory.getLogger("com.portfolio");

    /**
     * Used by the front end to fetch the number of evidence for each category
     * @param groupId The id of the group that data is requested for
     * @return A map of category names to the number of times they're used
     */
    @GetMapping("/group-{groupId}-categoriesData")
    public @ResponseBody Map<String, Integer> getCategoriesData(@PathVariable int groupId) {
        Group group = new Group(groupsClientService.getGroupDetailsById(groupId));
        return groupChartDataService.getGroupCategoryInfo(group);
    }


    /**
     * Used by the front end to fetch the number of evidence for each group member
     * @param groupId The id of the group that data is requested for
     * @return A map of group member ids + first and last names and the number of evidence for each.
     */
    @GetMapping("/group-{groupId}-membersData")
    public @ResponseBody Map<String, Integer> getEvidenceDataCompareGroupMembers(@PathVariable int groupId) {
        Group group = new Group(groupsClientService.getGroupDetailsById(groupId));
        return groupChartDataService.getGroupEvidenceDataCompareMembers(group);
    }


    /**
     * Used by the front end to fetch the number of evidence per
     * @param groupId The id of the group that data is requested for
     * @param timeRange The time range either (day, week or month)
     * @return A map of group member ids + first and last names and the number of evidence for each.
     */
    @GetMapping("/group-{groupId}-{timeRange}-{startDateString}-{endDateString}-dataOverTime")
    public @ResponseBody Map<String, Integer> getEvidenceDataOverTime(@PathVariable int groupId,
                                                                      @PathVariable String timeRange,
                                                                      @PathVariable String startDateString,
                                                                      @PathVariable String endDateString) throws ParseException {
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
        return groupChartDataService.getGroupEvidenceDataOverTime(group, startDate, endDate, timeRange);
    }
}
