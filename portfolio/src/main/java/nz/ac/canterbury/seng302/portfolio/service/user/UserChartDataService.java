package nz.ac.canterbury.seng302.portfolio.service.user;

import com.google.common.annotations.VisibleForTesting;
import nz.ac.canterbury.seng302.portfolio.model.evidence.PortfolioEvidence;
import nz.ac.canterbury.seng302.portfolio.model.group.Group;
import nz.ac.canterbury.seng302.portfolio.model.user.User;
import nz.ac.canterbury.seng302.portfolio.service.evidence.EvidenceService;
import nz.ac.canterbury.seng302.portfolio.util.DateComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserChartDataService {

    @Autowired
    private EvidenceService evidenceService;


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

    private static final Logger PORTFOLIO_LOGGER = LoggerFactory.getLogger("com.portfolio");


    /**
     * Gets the number of skills a user has across all pieces of evidence in a project within a date range
     * and returns the 10 skills with the highest frequency within this set.
     * @param userId The ID of the user
     * @param parentProjectId The ID of the project
     * @param startDate The start date, evidence created before this date won't be included
     * @param endDate The end date, evidence created before this date won't be included
     * @return A Map of skills represented by a capitalised string and an integer representing the frequency.
     */
    public Map<String, Integer> getUserSkillData(int userId, int parentProjectId, Date startDate, Date endDate) {
        Map<String, Integer> skillCounts = new HashMap<>();

        for (PortfolioEvidence e : evidenceService.getEvidenceForPortfolio(userId, parentProjectId)) {
            if(!startDate.after(e.getDate()) && !endDate.before(e.getDate())) {
                //Iterate through all skills within that piece of evidence
                for(String skill : e.getSkills()) {
                    skillCounts.merge(skill.toUpperCase(), 1, Integer::sum);
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
     * Get user evidence data over time within a project
     * @param user getting data for given user
     * @param parentProjectId select project id
     * @param startDate of evidence refinement
     * @param endDate of evidence refinement
     * @return a map of values.
     */
    public Map<String, Integer> getUserEvidenceData(User user, int parentProjectId, Date startDate, Date endDate) {
        TreeMap<String, Integer> evidenceCountOverTime = new TreeMap<>(new DateComparator());
        evidenceCountOverTime = getEvidenceOverTimeDay(evidenceCountOverTime, startDate, endDate, user, parentProjectId);
        return evidenceCountOverTime;
    }


    /**
     * Helper function for which creates a map of the total evidence produced by all group members
     * over time for each day within the start and end dates
     * @param evidenceCountOverTime the Map of all date strings in form ("yyyy-mm-dd") and the integer value of evidence
     * @param startDate The start date
     * @param endDate The end date
     * @param user the user object that the data is wanted for
     * @param parentProjectId The project id of the current project so the correct evidence is used
     */
    public TreeMap<String, Integer> getEvidenceOverTimeDay(TreeMap<String, Integer> evidenceCountOverTime, Date startDate, Date endDate, User user, int parentProjectId) {
        LocalDate start = Instant.ofEpochMilli(startDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate finish = Instant.ofEpochMilli(endDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();

        //Populate the map with all days within the bounds with key value 0
        for(LocalDate date = start; date.isBefore(finish.plusDays(1)); date = date.plusDays(1)) {
            evidenceCountOverTime.put(date.toString(), 0);
        }

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

        return evidenceCountOverTime;
    }
}

