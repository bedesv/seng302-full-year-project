package nz.ac.canterbury.seng302.portfolio.service.user;

import com.google.common.annotations.VisibleForTesting;
import nz.ac.canterbury.seng302.portfolio.model.evidence.PortfolioEvidence;
import nz.ac.canterbury.seng302.portfolio.service.evidence.EvidenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}

