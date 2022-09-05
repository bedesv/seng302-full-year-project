package nz.ac.canterbury.seng302.portfolio.service.evidence;

import nz.ac.canterbury.seng302.portfolio.model.evidence.Categories;
import nz.ac.canterbury.seng302.portfolio.model.evidence.Commit;
import nz.ac.canterbury.seng302.portfolio.model.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.model.evidence.PortfolioEvidence;
import nz.ac.canterbury.seng302.portfolio.model.evidence.WebLink;
import nz.ac.canterbury.seng302.portfolio.model.project.Project;
import nz.ac.canterbury.seng302.portfolio.model.user.User;
import nz.ac.canterbury.seng302.portfolio.repository.evidence.EvidenceRepository;
import nz.ac.canterbury.seng302.portfolio.service.project.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.user.UserAccountClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EvidenceService {

    @Autowired
    private EvidenceRepository repository;

    @Autowired
    private UserAccountClientService userService;

    @Autowired
    private ProjectService projectService;

    private static final Logger PORTFOLIO_LOGGER = LoggerFactory.getLogger("com.portfolio");

    /**
     * Updates a user's evidence with new skills.
     * Skills are space separated in order, i.e. old new old2 new2 old3 new3 ...
     *
     * @param userId The user of this portfolio
     * @param projectId The project for this portfolio
     * @param skillsToChange A string in form 'old new old2 new2 old3 new3' stating skills to change
     */
    public void updateEvidenceSkills(int userId, int projectId, String skillsToChange) {
        if (Objects.equals(skillsToChange, "")) {
            return; // No need to update skills if there are none to change.
        }
        List<Evidence> evidenceList = repository.findByOwnerIdAndProjectIdOrderByDateDescIdDesc(userId, projectId);
        List<String> skills = getSkillsFromEvidence(evidenceList);
        for (int i = 0; i < skills.size(); i += 1) {
            skills.set(i, skills.get(i).toLowerCase());
        }
        List<String> skillsList = new ArrayList<>(Arrays.asList(skillsToChange.split("\\s+")));
        List<List<String>> skillChanges = new ArrayList<>();
        for (int i = 1; i < skillsList.size(); i += 2) {
            String old = skillsList.get(i - 1);
            String changed = skillsList.get(i);
            // Either a skill is having its capitalization changed, or the changed skill should not already be a skill
            if (old.equalsIgnoreCase(changed) || !skills.contains(changed.toLowerCase())) {
                List<String> skillPair = new ArrayList<>();
                skillPair.add(old);
                skillPair.add(changed);
                skillChanges.add(skillPair);
            }
        }
        for (Evidence evidence : evidenceList) {
            evidence.changeSkills(skillChanges);
        }
    }

    public List<PortfolioEvidence> convertEvidenceForPortfolio(List<Evidence> evidenceList) {
        List<PortfolioEvidence> portfolioEvidenceList = new ArrayList<>();
        for (Evidence evidence: evidenceList) {
            List<User> userList = new ArrayList<>();
            for (int linkedUserId: evidence.getLinkedUsers()) {
                userList.add(userService.getUserAccountById(linkedUserId));
            }
            portfolioEvidenceList.add(new PortfolioEvidence(evidence, userList));
        }
        return portfolioEvidenceList;
    }
    /**
     * Get list of all pieces of evidence for a specific portfolio.
     * Portfolios can be identified by a user and project.
     * @param userId The user of this portfolio
     * @param projectId The project for this portfolio
     * @return A list of all evidence relating to this portfolio. It is ordered chronologically.
     */
    public List<PortfolioEvidence> getEvidenceForPortfolio(int userId, int projectId) {
        List<Evidence> evidenceList = repository.findByOwnerIdAndProjectIdOrderByDateDescIdDesc(userId, projectId);
        evidenceList.sort(Comparator.comparing(Evidence::getDate));
        Collections.reverse(evidenceList);
        List<PortfolioEvidence> portfolioEvidenceList =  convertEvidenceForPortfolio(evidenceList);
        String message = "Evidence for user " + userId + " and project " + projectId + " retrieved";
        PORTFOLIO_LOGGER.info(message);
        return portfolioEvidenceList;
    }

    /**
     * Get a specific piece of evidence by ID
     */
    public Evidence getEvidenceById(Integer id) throws NoSuchElementException {
        Optional<Evidence> evidence = repository.findById(id);
        if (evidence.isPresent()) {
            return evidence.get();
        }
        else {
            String message = "Evidence " + id + " not found";
            PORTFOLIO_LOGGER.error(message);
            throw new NoSuchElementException(message);
        }
    }

    public List<Evidence> getEvidenceByProjectId(int projectId) {
        return repository.findByProjectId(projectId);
    }

    /**
     * Saves a piece of evidence. Makes sure the project the evidence is for exists.
     * ALso makes sure the description and title are not one character, and contain at least one letter.
     * Also makes sure the project start and end dates are within the project bounds.
     * @param evidence The evidence to save
     */
    public void saveEvidence(Evidence evidence) {
        Project project;
        try {
            project = projectService.getProjectById(evidence.getProjectId());
        } catch (NoSuchElementException exception) {
            String message = "Evidence parent project " + evidence.getProjectId() + " not found";
            PORTFOLIO_LOGGER.error(message);
            throw new IllegalArgumentException(message);
        }
        Pattern fieldPattern = Pattern.compile("[a-zA-Z]+");
        Matcher titleMatcher = fieldPattern.matcher(evidence.getTitle());
        if (!titleMatcher.find() || evidence.getTitle().length() < 2 || evidence.getTitle().length() > 64) {
            String message = "Evidence title (" + evidence.getTitle() + ") is invalid";
            PORTFOLIO_LOGGER.error(message);
            throw new IllegalArgumentException("Title not valid");
        }
        Matcher descriptionMatcher = fieldPattern.matcher(evidence.getDescription());
        if (!descriptionMatcher.find() || evidence.getDescription().length() < 50 || evidence.getDescription().length() > 1024) {
            String message = "Evidence description (" + evidence.getDescription() + ") is invalid";
            PORTFOLIO_LOGGER.error(message);
            throw new IllegalArgumentException("Description not valid");
        }
        if (project.getStartDate().after(evidence.getDate()) || project.getEndDate().before(evidence.getDate())) {
            String message = "Evidence date (" + evidence.getDateString() + ") is invalid";
            PORTFOLIO_LOGGER.error(message);
            throw new IllegalArgumentException("Date not valid");
        }
        for (String skill : evidence.getSkills()) {
            if (skill.length() > 50) {
                String message = "Evidence skill (" + skill + ") is invalid";
                PORTFOLIO_LOGGER.error(message);
                throw new IllegalArgumentException("Skills not valid");
            }
        }
        List<Evidence> evidenceList = repository.findByOwnerIdAndProjectIdOrderByDateDescIdDesc(evidence.getOwnerId(), evidence.getProjectId());
        evidence.conformSkills(getSkillsFromEvidence(evidenceList));
        repository.save(evidence);
        String message = "Evidence " + evidence.getId() + " saved successfully";
        PORTFOLIO_LOGGER.info(message);
    }

    /**
     * Copies an evidence from the owner to the portfolio of a new user
     * Copies the skills, categories and weblinks of the evidence across as well
     * Conforms the skills to match the existing skills in the new user's portfolio
     * Throws illegal argument exception if the evidence does not exist
     * @param evidenceId  is the id of the evidence to be copied
     * @param userIdList is the list of ids of the users who get the copied evidence
     */
    public void copyEvidenceToNewUser(Integer evidenceId, List<Integer> userIdList) {
        try {
            Evidence evidence = getEvidenceById(evidenceId);
            String skillList;
            if (!evidence.getSkills().isEmpty()) {
                skillList = String.join(" ", evidence.getSkills());
            } else {
                skillList = "";
            }
            Set<Categories> categoriesSet = new HashSet<>(evidence.getCategories());
            for (Integer id: userIdList) {
                evidence.addLinkedUsers(id);
            }

            for (Integer userId: userIdList) {
                Evidence copiedEvidence = new Evidence(userId, evidence.getProjectId(), evidence.getTitle(), evidence.getDescription(), evidence.getDate(), skillList);
                copiedEvidence.setCategories(categoriesSet);

                for (WebLink webLink : evidence.getWebLinks()) {
                    copiedEvidence.addWebLink(webLink);
                }
                for (Commit commit : evidence.getCommits()) {
                    copiedEvidence.addCommit(commit);
                }
                for (Integer linkedUserId: evidence.getLinkedUsers()) {
                    copiedEvidence.addLinkedUsers(linkedUserId);
                }

                // This is to make sure that there are no duplicate skills in the other user's portfolio
                List<Evidence> evidenceList = repository.findByOwnerIdAndProjectIdOrderByDateDescIdDesc(copiedEvidence.getOwnerId(), copiedEvidence.getProjectId());
                copiedEvidence.conformSkills(getSkillsFromEvidence(evidenceList));
                String message = "Evidence " + evidenceId + " copied to " + userId + "'s portfolio";
                PORTFOLIO_LOGGER.info(message);
                repository.save(copiedEvidence);
            }
        } catch (NoSuchElementException e) {
            String message = "Evidence " + evidenceId + " not found";
            PORTFOLIO_LOGGER.error(message);
            throw new NoSuchElementException(e.getMessage());
        }
    }

    /**
     * Gets all skills from a list of evidence. Each skill returned is unique.
     * @param evidenceList A list of evidence to retrieve skills from.
     * @return All the skills for that list of evidence.
     */
    public List<String> getSkillsFromEvidence(List<Evidence> evidenceList) {
        Set<String> skills = new HashSet<>();
        for (Evidence userEvidence : evidenceList) {
            skills.addAll(userEvidence.getSkills());
        }
        List<String> skillList = new ArrayList<>(skills);
        skillList.sort(String::compareToIgnoreCase);
        return skillList;
    }

    /**
     * Gets all skills from a list of portfolio evidence. Each skill returned is unique.
     * @param evidenceList A list of evidence to retrieve skills from.
     * @return All the skills for that list of evidence.
     */
    public Collection<String> getSkillsFromPortfolioEvidence(List<PortfolioEvidence> evidenceList) {
        Collection<String> skills = new HashSet<>();
        for (PortfolioEvidence userEvidence : evidenceList) {
            skills.addAll(userEvidence.getSkills());
        }
        List<String> skillList = new ArrayList<>(skills);
        skillList.sort(String::compareToIgnoreCase);
        return skillList;
    }

    /**
     * Deletes a piece of evidence.
     * @param id The ID of the evidence to delete
     */
    public void deleteById(int id) {
        try {
            repository.deleteById(id);
            String message = "Deleted evidence: " + id;
            PORTFOLIO_LOGGER.info(message);
        } catch(Exception exception) {
            String message = "Evidence " + id + " not found";
            PORTFOLIO_LOGGER.error(message);
            throw new IllegalArgumentException(message);
        }
    }


    /**
     * Saves a web link string to the evidence specified by evidenceId.
     * @param evidenceId The evidence to have the web link added to.
     * @param weblink The web link sting to be added to evidence of id=evidenceId.
     * @throws NoSuchElementException If evidence specified by evidenceId does not exist NoSuchElementException
     * is thrown.
     */
    public void saveWebLink(int evidenceId, WebLink weblink) throws NoSuchElementException {
            try {
                Evidence evidence = getEvidenceById(evidenceId);
                evidence.addWebLink(weblink);
                saveEvidence(evidence);
                String message = "Evidence web link " + weblink.getName() + " saved successfully";
                PORTFOLIO_LOGGER.info(message);
            } catch (NoSuchElementException e) {
                String message = "Evidence " + evidenceId + " not found. Weblink not saved";
                PORTFOLIO_LOGGER.error(message);
                throw new NoSuchElementException("Evidence not found: web link not saved");
            }
    }

    /**
     * Updates a web link string to the evidence specified by evidenceId.
     * @param evidenceId The evidence to have the web link added to.
     * @param weblink The web link sting to be added to evidence of id=evidenceId.
     * @param index The index of the weblink list to update
     * @throws NoSuchElementException If evidence specified by evidenceId does not exist NoSuchElementException
     * is thrown.
     */
    public void modifyWebLink(int evidenceId, WebLink weblink, int index) throws NoSuchElementException {
        try {
            Evidence evidence = getEvidenceById(evidenceId);
            evidence.addWebLinkWithIndex(weblink, index);
            saveEvidence(evidence);
            String message = "Evidence weblink" + weblink.getName() + " saved successfully";
            PORTFOLIO_LOGGER.info(message);
        } catch (NoSuchElementException e) {
            String message = "Evidence " + evidenceId + " not found. Weblink not saved";
            PORTFOLIO_LOGGER.error(message);
            throw new NoSuchElementException("Evidence not found: web link not saved");
        }
    }

    /**
     * Saves a commit to the evidence specified by evidenceId.
     * @param evidenceId The ID of the evidence to have the commit added to.
     * @param commit The commit object added to the evidence
     * @throws NoSuchElementException If evidence specified by evidenceId does not exist NoSuchElementException
     * is thrown.
     */
    public void saveCommit(int evidenceId, Commit commit) throws NoSuchElementException {
        try {
            Evidence evidence = getEvidenceById(evidenceId);
            evidence.addCommit(commit);
            saveEvidence(evidence);
            String message = "Evidence commit saved successfully";
            PORTFOLIO_LOGGER.info(message);
        } catch (NoSuchElementException e) {
            String message = "Evidence " + evidenceId + " not found. Commit not saved";
            PORTFOLIO_LOGGER.error(message);
            throw new NoSuchElementException("Evidence not found: commit not saved");
        }
    }

    /**
     * Checks that a weblink is in the correct format
     * @param weblink The string representation of the weblink being validated
     * @throws IllegalArgumentException If the weblink is in the wrong format IllegalArgumentException is thrown
     */
    public void validateWebLink(String weblink) {
        Pattern fieldPattern = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
        Matcher weblinkMatcher = fieldPattern.matcher(weblink);
        if (!weblinkMatcher.find()) {
            String message = "Evidence weblink" + weblink + " in invalid format";
            PORTFOLIO_LOGGER.error(message);
            throw new IllegalArgumentException("Weblink not in valid format");
        }
        try {
            new URL(weblink).toURI();
        } catch (Exception e) {
            throw new IllegalArgumentException("Weblink not in valid format");
        }
    }

    /**
     * Wrote a method for retrieve evidence by skill
     * @param skill being searched for
     * @return list of evidences containing skill
     */
    public List<Evidence> retrieveEvidenceBySkill(String skill, int projectId) {
        return repository.findBySkillsAndProjectIdOrderByDateDescIdDesc(skill, projectId);
    }

    /**
     * Retrieve all evidence for a project where skills are null
     * @param projectId of evidence
     * @return list of evidences with no skill
     */
    public List<Evidence> retrieveEvidenceWithNoSkill(int userId, int projectId){
        return repository.findByOwnerIdAndProjectIdAndSkillsIsNullOrderByDateDescIdDesc(userId, projectId);
    }

    /**
     * Retrieves all evidence owned by the given user user and with the given skill
     * @param skill The skill being searched for
     * @param userId The owner of the Evidence
     * @return A list of evidence owned by the user and containing the skill
     */
    public List<Evidence> retrieveEvidenceBySkillAndUser(String skill, int userId, int projectId) {
        List<Evidence> usersEvidenceWithSkill = new ArrayList<>();
        for (Evidence e : retrieveEvidenceBySkill(skill, projectId)) {
            if (e.getOwnerId() == userId) {
                usersEvidenceWithSkill.add(e);
            }
        }
        return usersEvidenceWithSkill;
    }
    /**
     * Service method for setting the categories of a piece of evidence.
     * @param categories A set of Categories enum (QUANTITATIVE, QUALITATIVE, SERVICE)
     */
    public void setCategories(Set<Categories> categories, Evidence evidence) {
        evidence.setCategories(categories);
        saveEvidence(evidence);
    }

    /**
     * Evidence service method for getting a list of evidence with a given user, project and category
     * @param userId The user ID
     * @param projectId The project ID
     * @param categorySelection The Category selection
     * @return List of evidence with the given parameters
     */
    public List<Evidence> getEvidenceByCategoryForPortfolio(int userId, int projectId, Categories categorySelection) {

        List<Evidence> evidenceList = repository.findByOwnerIdAndProjectIdOrderByDateDescIdDesc(userId, projectId);
        List<Evidence> evidenceListWithCategory = new ArrayList<>();
        for(Evidence e: evidenceList) {
            if(e.getCategories().contains(categorySelection)) {
                evidenceListWithCategory.add(e);
            }
        }
        return evidenceListWithCategory;
    }

    /**
     * Retrieves all evidence for a project where the category is null
     * @param projectId of evidence
     * @return list of evidence with no category.
     */
    public List<Evidence> retrieveEvidenceWithNoCategory(int userId, int projectId) {
        return repository.findByOwnerIdAndProjectIdAndCategoriesIsNullOrderByDateDescIdDesc(userId, projectId);
    }
}