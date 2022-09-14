package nz.ac.canterbury.seng302.portfolio.service.group;

import com.google.common.annotations.VisibleForTesting;
import nz.ac.canterbury.seng302.portfolio.model.evidence.Categories;
import nz.ac.canterbury.seng302.portfolio.model.evidence.PortfolioEvidence;
import nz.ac.canterbury.seng302.portfolio.model.group.Group;
import nz.ac.canterbury.seng302.portfolio.model.project.DateRefineOption;
import nz.ac.canterbury.seng302.portfolio.model.project.Project;
import nz.ac.canterbury.seng302.portfolio.model.project.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.user.User;
import nz.ac.canterbury.seng302.portfolio.service.evidence.EvidenceService;
import nz.ac.canterbury.seng302.portfolio.service.project.SprintService;
import nz.ac.canterbury.seng302.portfolio.util.DateComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GroupChartDataService {

    @Autowired
    private EvidenceService evidenceService;

    @Autowired
    private SprintService sprintService;

    /**
     * Iterates through all group members then the members' pieces of evidence then the skills within those evidence
     * to count the occurrence of skills within all pieces of evidence for that group
     * @param group the group object that the data is wanted for
     * @param startDate The start date, evidence must be created on or after this date.
     * @param endDate The end date, evidence must be created on or before this date.
     * @return A map of skills and their occurrence in the group's pieces of evidence
     */
    public Map<String, Integer> getGroupSkillData(Group group, Date startDate, Date endDate) {
        int parentProjectId = group.getParentProject();

        Map<String, Integer> skillCounts = new HashMap<>();
        // Iterate through every user in the group
        for (User user : group.getMembers()) {
            // Iterate through all of that user's evidence for the groups project
            for (PortfolioEvidence e : evidenceService.getEvidenceForPortfolio(user.getId(), parentProjectId)) {
                if(!startDate.after(e.getDate()) && !endDate.before(e.getDate())) {
                    //Iterate through all skills within that piece of evidence
                    for(String skill : e.getSkills()) {
                        skillCounts.merge(skill.toUpperCase(), 1, Integer::sum);
                    }
                }
            }
        }
        // Sorts map by skill amount and caps the new map at 10 elements.
        return skillCounts.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }


    /**
     * Iterates through the groups members then the members' pieces of evidence
     * to count up the occurrences of each category
     * @param group the group object that the data is wanted for
     * @return A map of categories to their occurrences in the group's pieces of evidence
     */
    public Map<String, Integer> getGroupCategoryInfo(Group group, Date startDate, Date endDate) {
        int parentProjectId = group.getParentProject();

        // Initialise the hashmap to have a count of 0 for every category
        Map<String, Integer> categoryCounts = new HashMap<>();
        categoryCounts.put("Service", 0);
        categoryCounts.put("Quantitative", 0);
        categoryCounts.put("Qualitative", 0);
        // Iterate through every user in the group
        for (User user : group.getMembers()) {
            // Iterate through all of that user's evidence for the groups project
            for (PortfolioEvidence e : evidenceService.getEvidenceForPortfolio(user.getId(), parentProjectId)) {
                if(!startDate.after(e.getDate()) && !endDate.before(e.getDate())) {
                    // Increase the count of the category by 1 if the piece of evidence has that category
                    if (e.getCategories().contains(Categories.SERVICE)) {
                        categoryCounts.merge("Service", 1, Integer::sum);
                    }
                    if (e.getCategories().contains(Categories.QUANTITATIVE)) {
                        categoryCounts.merge("Quantitative", 1, Integer::sum);
                    }
                    if (e.getCategories().contains(Categories.QUALITATIVE)) {
                        categoryCounts.merge("Qualitative", 1, Integer::sum);
                    }
                }
            }
        }
        return categoryCounts;
    }

    /**
     * Iterates through the groups members then the members' pieces of evidence
     * to count up the number of evidence per group members
     * @param group the group object that the data is wanted for
     * @return A map of group members (ID, First name and Last name) and the number of evidence
     */
    public Map<String, Integer> getGroupEvidenceDataCompareMembers(Group group, Date startDate, Date endDate) {
        int parentProjectId = group.getParentProject();
        Map<String, Integer> evidenceCountsByMember = new HashMap<>();

        // Iterate through every user in the group and add the number of evidence they have produced for the given project
        for (User user : group.getMembers()) {
            evidenceCountsByMember.put(user.getId() + " " + user.getFullName(), 0);
            // Iterate through all of that user's evidence for the groups project
            for (PortfolioEvidence e : evidenceService.getEvidenceForPortfolio(user.getId(), parentProjectId)) {
                if (!startDate.after(e.getDate()) && !endDate.before(e.getDate())) {
                    evidenceCountsByMember.merge(user.getId() + " " + user.getFullName(), 1, Integer::sum);;
                }
            }
        }

        // Sorts map by group members evidence amount and caps the new map at 10 elements.
        return evidenceCountsByMember.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    /**
     * Returns a map of the number of evidence total of all group members over time
     * @param group the group object that the data is wanted for
     * @return A map of dates either (day, week (sunday), month) and the number of evidence produced within those dates
     */
    public Map<String, Integer> getGroupEvidenceDataOverTime(Group group, Date startDate, Date endDate, String timeRange) {
        int parentProjectId = group.getParentProject();

        final TreeMap<String, Integer> evidenceCountOverTime = new TreeMap<>(new DateComparator());
        //Map<String, Integer> evidenceCountOverTime = new HashMap<>();

        //check granularity
        if (Objects.equals(timeRange, "day")) {
            getEvidenceOverTimeDay(evidenceCountOverTime, startDate, endDate, group, parentProjectId);
        } else if (Objects.equals(timeRange, "week")) {
            getEvidenceOverTimeWeek(evidenceCountOverTime, startDate, endDate, group, parentProjectId);
        }

        return evidenceCountOverTime;
    }

    /**
     * Helper function for getGroupEvidenceOverTime which creates a map of the total evidence produced by all group members
     * over time for each day within the start and end dates
     * @param evidenceCountOverTime the Map of all date strings in form ("yyyy-mm-dd") and the integer value of evidence
     * @param startDate The start date
     * @param endDate The end date
     * @param group the group object that the data is wanted for
     * @param parentProjectId The parent project of the group so the correct evidence is used
     */
    public void getEvidenceOverTimeDay(Map<String, Integer> evidenceCountOverTime, Date startDate, Date endDate, Group group, int parentProjectId) {
        LocalDate start = Instant.ofEpochMilli(startDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate finish = Instant.ofEpochMilli(endDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();

        //Populate the map with all days within the bounds with key value 0
        for(LocalDate date = start; date.isBefore(finish.plusDays(1)); date = date.plusDays(1)) {
            evidenceCountOverTime.put(date.toString(), 0);
        }
        for (User user : group.getMembers()) {
            // Iterate through all of that user's evidence for the groups project
            for (PortfolioEvidence e : evidenceService.getEvidenceForPortfolio(user.getId(), parentProjectId)) {
                if (!startDate.after(e.getDate()) && !endDate.before(e.getDate())) {
                    // Iterate through all of that user's evidence for the groups project and if the evidence falls on one of the days
                    // mentioned above add 1 to that day
                    LocalDate evidenceDate = Instant.ofEpochMilli(e.getDate().getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
                    if (evidenceDate.isAfter(start.minusDays(1)) && evidenceDate.isBefore(finish.plusDays(1))) {
                        evidenceCountOverTime.merge(evidenceDate.toString(), 1, Integer::sum);
                    }
                }
            }
        }
    }

    /**
     * Helper function for getGroupEvidenceOverTime which creates a map of the total evidence produced by all group members
     * over time for each week (Sunday date) within the start and end dates
     * @param evidenceCountOverTime the Map of all date strings in form ("yyyy-mm-dd") and the integer value of evidence
     * @param startDate The start date
     * @param endDate The end date
     * @param group the group object that the data is wanted for
     * @param parentProjectId The parent project of the group so the correct evidence is used
     */
    public void getEvidenceOverTimeWeek(Map<String, Integer> evidenceCountOverTime, Date startDate, Date endDate, Group group, int parentProjectId) {
        LocalDate start = Instant.ofEpochMilli(startDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate finish = Instant.ofEpochMilli(endDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();

        //Populate the map with all the weeks inclusive from the start date to the end date with a value of zero
        //Weeks are represented by the date of Sunday of that week
        for(LocalDate date = start.with(DayOfWeek.SUNDAY); date.isBefore(finish.with(DayOfWeek.SUNDAY).plusDays(1)); date = date.plusDays(7)) {
            evidenceCountOverTime.put(date.toString(), 0);
        }
        for (User user : group.getMembers()) {
            // Iterate through all of that user's evidence for the groups project and if the evidence falls in one of the weeks
            // mentioned above add 1 to the value for that week
            for (PortfolioEvidence e : evidenceService.getEvidenceForPortfolio(user.getId(), parentProjectId)) {
                LocalDate evidenceDate = Instant.ofEpochMilli(e.getDate().getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
                if (evidenceDate.isAfter(start.minusDays(1)) && evidenceDate.isBefore(finish.plusDays(1))) {
                    evidenceCountOverTime.merge(evidenceDate.with(DayOfWeek.SUNDAY).toString(), 1, Integer::sum);
                }
            }
        }
    }

        /**
         * Only for mocking purposes
         * Updates the current EvidenceService with a new one
         * @param newEvidenceService the new (mocked) EvidenceService
         */
    @VisibleForTesting
    protected void setEvidenceService(EvidenceService newEvidenceService) {
        evidenceService = newEvidenceService;
    }

    /**
     * Only for mocking purposes
     * @return the current evidence service
     */
    @VisibleForTesting
    protected EvidenceService getEvidenceService() {
        return evidenceService;
    }

    /**
     * Set dropdown options for the
     * @param model Global model
     * @param project current project
     */
    public void setDateRefiningOptions (Model model, Project project) {
        List<DateRefineOption> dateRefineOptions = new ArrayList<>();
        dateRefineOptions.add(new DateRefineOption("Whole Project", project.getStartDate(), project.getEndDate()));
        List<Sprint> sprints = sprintService.getSprintsByProjectInOrder(project.getId());
        if (!sprints.isEmpty()){
            for (Sprint sprint : sprints) {
                dateRefineOptions.add(new DateRefineOption(sprint.getLabel(), sprint.getStartDate(), sprint.getEndDate()));
            }
        }
        model.addAttribute("dateRefiningOptions", dateRefineOptions);
    }
}
