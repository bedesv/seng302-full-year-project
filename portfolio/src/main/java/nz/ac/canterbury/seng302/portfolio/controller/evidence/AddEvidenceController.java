package nz.ac.canterbury.seng302.portfolio.controller.evidence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import nz.ac.canterbury.seng302.portfolio.model.evidence.Categories;
import nz.ac.canterbury.seng302.portfolio.model.evidence.Commit;
import nz.ac.canterbury.seng302.portfolio.model.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.model.evidence.WebLink;
import nz.ac.canterbury.seng302.portfolio.model.evidence.PortfolioEvidence;
import nz.ac.canterbury.seng302.portfolio.model.group.Group;
import nz.ac.canterbury.seng302.portfolio.model.project.Project;
import nz.ac.canterbury.seng302.portfolio.model.user.User;
import nz.ac.canterbury.seng302.portfolio.service.evidence.EvidenceService;
import nz.ac.canterbury.seng302.portfolio.service.group.GitlabConnectionService;
import nz.ac.canterbury.seng302.portfolio.service.group.GroupRepositorySettingsService;
import nz.ac.canterbury.seng302.portfolio.service.group.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.project.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.project.SprintService;
import nz.ac.canterbury.seng302.portfolio.service.user.*;
import nz.ac.canterbury.seng302.portfolio.util.ValidationUtil;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The controller for handling backend of the add evidence page
 */
@Controller
public class AddEvidenceController {

    private static final String ADD_EVIDENCE = "templatesEvidence/addEvidence";
    private static final String PORTFOLIO_REDIRECT = "redirect:/portfolio";

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SprintService sprintService;

    @Autowired
    private UserAccountClientService userService;

    @Autowired
    private GroupRepositorySettingsService groupRepositorySettingsService;

    @Autowired
    private GroupsClientService groupsService;

    @Autowired
    private PortfolioUserService portfolioUserService;

    @Autowired
    private EvidenceService evidenceService;

    @Autowired
    private GitlabConnectionService gitlabConnectionService;

    private static final String TIMEFORMAT = "yyyy-MM-dd";

    private static final Logger PORTFOLIO_LOGGER = LoggerFactory.getLogger("com.portfolio");

    private static final int MAX_WEBLINKS_PER_EVIDENCE = 5;

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Display the add evidence page.
     * @param principal Authentication state of client
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return The add evidence page.
     */
    @GetMapping("/editEvidence-{evidenceId}")
    public String addEvidence(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("evidenceId") String evidenceId,
            Model model
    ) {

        int userId = userService.getUserId(principal);
        int projectId = portfolioUserService.getUserById(userId).getCurrentProject();
        if (projectId == -1) {
            model.addAttribute("errorMessage", "Please select a project first");
            return PORTFOLIO_REDIRECT;
        }
        Project project = projectService.getProjectById(projectId);

        try {
            Evidence evidence = getEvidenceById(evidenceId, userId, projectId);
            addEvidenceToModel(model, projectId, userId, evidence);
            model.addAttribute("minEvidenceDate", Project.dateToString(project.getStartDate(), TIMEFORMAT));
            model.addAttribute("maxEvidenceDate", Project.dateToString(project.getEndDate(), TIMEFORMAT));
            model.addAttribute("evidenceId", Integer.parseInt(evidenceId));
            model.addAttribute("maxWeblinks", MAX_WEBLINKS_PER_EVIDENCE);
            return ADD_EVIDENCE;
        } catch (IllegalArgumentException e) {
            return PORTFOLIO_REDIRECT;
        }
    }

    /**
     * Save a piece of evidence. It will be rejected silently if the data given is invalid, otherwise it will be saved.
     * If saved, the user will be taken to their portfolio page.
     * @param principal Authentication state of client
     * @param title The title of the piece of evidence
     * @param description The description of the piece of evidence
     * @param dateString The date the evidence occurred, in yyyy-MM-dd string format
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return A redirect to the portfolio page, or staying on the add evidence page
     */
    @PostMapping("/editEvidence-{evidenceId}")
    public String saveEvidence(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("evidenceId") String evidenceId,
            @RequestParam(name="evidenceTitle") String title,
            @RequestParam(name="evidenceDescription") String description,
            @RequestParam(name="evidenceDate") String dateString,
            @RequestParam(name="isQuantitative", required = false) String isQuantitative,
            @RequestParam(name="isQualitative", required = false) String isQualitative,
            @RequestParam(name="isService", required = false) String isService,
            @RequestParam(name="evidenceSkills") String skills,
            @RequestParam(name="skillsToChange") String skillsToChange,
            @RequestParam(name="evidenceUsers") String users,
            @RequestParam(name="evidenceCommits") String commitString,
            @RequestParam(required = false, name="evidenceWebLinks") List<String> webLinkLinks,
            @RequestParam(required = false, name="evidenceWebLinkNames") List<String> webLinkNames,
            Model model
    ) {

        User user = userService.getUserAccountByPrincipal(principal);
        int projectId = portfolioUserService.getUserById(user.getId()).getCurrentProject();
        Project project = projectService.getProjectById(projectId);

        model.addAttribute("minEvidenceDate", Project.dateToString(project.getStartDate(), TIMEFORMAT));
        model.addAttribute("maxEvidenceDate", Project.dateToString(project.getEndDate(), TIMEFORMAT));

        Date date;
        try {
            date = new SimpleDateFormat(TIMEFORMAT).parse(dateString);
        } catch (ParseException exception) {
            return PORTFOLIO_REDIRECT; // Fail silently as client has responsibility for error checking
        }

        Set<Categories> categories = new HashSet<>();
        if (isQuantitative != null) {
            categories.add(Categories.QUANTITATIVE);
        }
        if (isQualitative != null) {
            categories.add(Categories.QUALITATIVE);
        }
        if (isService != null) {
            categories.add(Categories.SERVICE);
        }

        int userId = user.getId();
        evidenceService.updateEvidenceSkills(userId, projectId, skillsToChange);
        Evidence evidence = getEvidenceById(evidenceId, userId, projectId);
        evidence.setTitle(title);
        evidence.setDescription(description);
        evidence.setSkills(skills);
        evidence.setDate(date);
        evidence.setCategories(categories);

        List<Boolean> validationResponse = evidenceService.validateEvidence(model, title, description, evidence.getSkills());
        if (validationResponse.contains(false)){
            evidence.setTitle(ValidationUtil.stripTitle(title));
            evidence.setDescription(ValidationUtil.stripTitle(description));
            evidence.setSkills(evidenceService.stripSkills(evidence.getSkills()));
            addEvidenceToModel(model, projectId, userId, evidence);
            return ADD_EVIDENCE;
        }

        // Format commit string into valid list
        if (Objects.equals(commitString, "")) {
            commitString = "[]";
        }
        try {
            List<Commit> commitList =
                    mapper.readValue(commitString, new TypeReference<>() {});
            evidence.setCommits(commitList);
        } catch (JsonProcessingException e) {
            PORTFOLIO_LOGGER.info(e.getMessage());
            addEvidenceToModel(model, projectId, userId, evidence);
            return PORTFOLIO_REDIRECT; // Fail silently as client has responsibility for error checking
        }

        try {
            addWebLinksToEvidence(evidence, webLinkLinks, webLinkNames);
            System.out.println(evidence.getWebLinks());
            evidenceService.saveEvidence(evidence);
        } catch (IllegalArgumentException exception) {
            if (Objects.equals(exception.getMessage(), "Title not valid")) {
                model.addAttribute("titleError", "Title cannot be all special characters");
            } else if (Objects.equals(exception.getMessage(), "Description not valid")) {
                model.addAttribute("descriptionError", "Description cannot be all special characters");
            } else if (Objects.equals(exception.getMessage(), "Date not valid")) {
                model.addAttribute("dateError", "Date must be within the project dates");
            } else if (Objects.equals(exception.getMessage(), "Skills not valid")) {
                model.addAttribute("skillsError", "Skills cannot be more than 50 characters long");
            } else if (Objects.equals(exception.getMessage(), "Weblink not in valid format")) {
                model.addAttribute("webLinkError", "Weblink is invalid");
            } else {
                model.addAttribute("generalError", exception.getMessage());
            }
            evidence.setTitle(ValidationUtil.stripTitle(title));
            evidence.setDescription(ValidationUtil.stripTitle(description));
            evidence.setSkills(evidenceService.stripSkills(evidence.getSkills()));
            addEvidenceToModel(model, projectId, userId, evidence);
            return ADD_EVIDENCE; // Fail silently as client has responsibility for error checking
        }
        try {
            evidenceService.updateEvidenceUsers(evidence, new HashSet<>(userService.getUserIdListFromString(users)));
        } catch (IllegalArgumentException exception) {
            return ADD_EVIDENCE;
        }
        return PORTFOLIO_REDIRECT;
    }

    private void addWebLinksToEvidence(Evidence evidence, List<String> webLinkLinks, List<String> webLinkNames) {
        List<WebLink> webLinks = new ArrayList<>();
        if (webLinkLinks != null && webLinkNames != null) {
            for (int i = 0; i < webLinkLinks.size(); i++) {
                evidenceService.validateWebLink(webLinkLinks.get(i));
                if (webLinkNames.get(i).isEmpty()) {
                    webLinks.add(new WebLink(webLinkLinks.get(i)));
                } else {
                    webLinks.add(new WebLink(webLinkLinks.get(i), webLinkNames.get(i)));
                }
            }
            if (!webLinks.isEmpty()) {
                evidence.setWebLinks(webLinks);
            }
        }
    }

    /**
     * Gets a piece of evidence based on an id in string form.
     * If the id is valid, that evidence is returned. If it is -1, a new piece is returned.
     * Else, an error is thrown.
     * @param evidenceId The id of the evidence
     * @param userId The id of the user who the evidence belongs to
     * @param projectId The id of the project the evidence belongs to
     * @return A piece of evidence
     */
    private Evidence getEvidenceById(String evidenceId, int userId, int projectId) {
        try {
            int id = Integer.parseInt(evidenceId);
            if (id == -1) {
                Date evidenceDate;
                Date currentDate = new Date();
                Project project = projectService.getProjectById(projectId);
                if (currentDate.after(project.getStartDate()) && currentDate.before(project.getEndDate())) {
                    evidenceDate = currentDate;
                } else {
                    evidenceDate = project.getStartDate();
                }
                return new Evidence(userId, projectId, "", "", evidenceDate);
            } else {
                Evidence evidence = evidenceService.getEvidenceById(id);
                if (userId != evidence.getOwnerId()) {
                    String errorMessage = "User " + userId + " tried to access evidence they did not own";
                    PORTFOLIO_LOGGER.error(errorMessage);
                    throw new IllegalArgumentException();
                }
                return evidence;
            }
        } catch (NumberFormatException e) {
            String errorMessage = "Not a number id in add evidence get request: " + evidenceId;
            PORTFOLIO_LOGGER.error(errorMessage);
            throw new IllegalArgumentException();
        } catch (NoSuchElementException e) {
            String errorMessage = "Non-existent id in add evidence get request: " + evidenceId;
            PORTFOLIO_LOGGER.error(errorMessage);
            throw new IllegalArgumentException();
        }
    }

    /**
     * Adds information to the model for the repositories of groups a user is in.
     * @param projectId The project the user has selected
     * @param userId The id of the user
     * @param model The model to add information to
     */
    private void addRepositoryInfoToModel(int projectId, int userId, int groupId, Model model) {
        List<Group> groups = groupsService.getAllGroupsUserIn(projectId, userId);
        List<Group> groupsWithCommits = new ArrayList<>();
        List<Commit> commitList = new ArrayList<>();
        List<Member> members = new ArrayList<>();
        List<Branch> branches = new ArrayList<>();
        Branch defaultBranch = null;
        Group mainGroup = null;
        Date startDate = null;
        Date actualEndDate = null;
        boolean displayCommits = false;
        if (!groups.isEmpty()) {
            // Try to set the selected group to the given group id
            for (Group g : groups) {
                if (gitlabConnectionService.repositoryHasCommits(g.getGroupId())) {
                    groupsWithCommits.add(g);
                    displayCommits = true;
                }
                if (g.getGroupId() == groupId) {
                    mainGroup = g;
                }
            }

            // If selected group is null, set the selected group to the first group in the list
            if (mainGroup == null) {
                mainGroup = groupsWithCommits.get(0);
            }

            List<org.gitlab4j.api.models.Commit> commits = new ArrayList<>();

            // Calculate the current date and the date two weeks ago as the default date range for the search
            Calendar calendar = Calendar.getInstance();
            actualEndDate = calendar.getTime();
            calendar.add(Calendar.DATE, 1); // Add one day because end date isn't inclusive
            Date endDate = calendar.getTime();
            calendar.add(Calendar.DATE, -15); // Take away 15 days to be two weeks before today
            startDate = calendar.getTime();

            // Retrieve commits, members and branches from the group's repository
            try {
                commits = gitlabConnectionService.getFilteredCommits(mainGroup.getGroupId(), startDate, endDate, "", "", "");
                members = gitlabConnectionService.getAllMembers(mainGroup.getGroupId());
                branches = gitlabConnectionService.getAllBranches(mainGroup.getGroupId());
            } catch (RuntimeException e) {
                PORTFOLIO_LOGGER.error(e.getMessage());
            }

            // Convert the retrieved commits to our own object
            for (org.gitlab4j.api.models.Commit commit : commits) {
                Commit portfolioCommit = new Commit(commit.getId(), commit.getCommitterName(),
                        commit.getCommittedDate(), commit.getWebUrl(), commit.getMessage());
                commitList.add(portfolioCommit);
            }

            // Figure out what the main branch of the repository is
            for (Branch branch : branches) {
                if (Boolean.TRUE.equals(branch.getDefault())) {
                    defaultBranch = branch;
                }
            }
        }

        // Sort the list of commits in reverse chronological order (newest first)
        commitList.sort(Comparator.comparing(Commit::getDate));
        Collections.reverse(commitList);

        // Add all the relevant objects to the page model
        model.addAttribute("commits", commitList);
        model.addAttribute("displayCommits", displayCommits);
        model.addAttribute("repositoryUsers", members);
        model.addAttribute("branches", branches);
        model.addAttribute("defaultBranch", defaultBranch);
        model.addAttribute("groups", groupsWithCommits);
        model.addAttribute("mainGroup", mainGroup);
        model.addAttribute("sprints", sprintService.getByParentProjectId(projectId));
        if (displayCommits) {
            model.addAttribute("startDate", Project.dateToString(startDate, TIMEFORMAT));
            model.addAttribute("endDate", Project.dateToString(actualEndDate, TIMEFORMAT));
        }
    }

    /**
     * Adds helpful evidence related variables to the model.
     * They are a title, description, date, and a list of all skills for the user.
     * @param model The model to add things to
     * @param projectId The project currently being viewed
     * @param userId The logged-in user
     * @param evidence The evidence that is being viewed.
     */
    private void addEvidenceToModel(Model model, int projectId, int userId, Evidence evidence) {
        List<PortfolioEvidence> evidenceList = evidenceService.getEvidenceForPortfolio(userId, projectId);
        model.addAttribute("categories", evidence.getCategories());
        model.addAttribute("skillsList", evidenceService.getSkillsFromPortfolioEvidence(evidenceList));
        model.addAttribute("evidenceTitle", evidence.getTitle());
        model.addAttribute("evidenceDescription", evidence.getDescription());
        model.addAttribute("evidenceDate", Project.dateToString(evidence.getDate(), TIMEFORMAT));
        model.addAttribute("evidenceSkills", String.join(" ", evidence.getSkills()) + " ");
        Set<Integer> linkedUsers = evidence.getLinkedUsers();
        linkedUsers.remove(userId);
        model.addAttribute("evidenceUsers", linkedUsers);
        try {
            model.addAttribute("evidenceCommits", mapper.writeValueAsString(evidence.getCommits()));
        } catch (JsonProcessingException e) { // Should never happen if application is set up correctly, but log an error just in case
            PORTFOLIO_LOGGER.error(e.getMessage());
        }
        model.addAttribute("users", userService.getAllUsersExcept(userId));
        addRepositoryInfoToModel(projectId, userId, -1, model);
    }

    /**
     * A method which deletes the evidence based on its id.
     * @return the portfolio page of the user
     */
    @DeleteMapping(value = "/deleteEvidence-{evidenceId}")
    public String deleteEvidenceById(
            @PathVariable(name="evidenceId") String evidenceId) {
        int id = Integer.parseInt(evidenceId);
        evidenceService.deleteById(id);
        return PORTFOLIO_REDIRECT;
    }

    @GetMapping(value="/evidenceCommitFilterBox")
    public String getUpdatedCommitFilterBox(@AuthenticationPrincipal AuthState principal, @RequestParam(name="groupId") int groupId, Model model) {
        User user = userService.getUserAccountByPrincipal(principal);
        int projectId = portfolioUserService.getUserById(user.getId()).getCurrentProject();
        addRepositoryInfoToModel(projectId, user.getId(), groupId, model);
        return "templatesEvidence/commitFilterBox";
    }

    @GetMapping(value="/searchFilteredCommits")
    public @ResponseBody List<Commit> searchFilteredCommits(@AuthenticationPrincipal AuthState principal,
                                              @RequestParam(name="groupId") int groupId,
                                              @RequestParam(name="startDate") String startDateString,
                                              @RequestParam(name="endDate") String endDateString,
                                              @RequestParam(name="branch") String branch,
                                              @RequestParam(name= "commitAuthor") String commitAuthor,
                                              @RequestParam(name="commitId") String commitId) {
        Date startDate = null;
        Date endDate = null;
        try {
            startDate = new SimpleDateFormat(TIMEFORMAT).parse(startDateString);
            endDate = new SimpleDateFormat(TIMEFORMAT).parse(endDateString);
            // Add one day to the end date so that both dates on the same day works properly
            Calendar cal = Calendar.getInstance();
            cal.setTime(endDate);
            cal.add(Calendar.DATE, 1);
            endDate = cal.getTime();
        } catch (Exception ignored) {
            // Date parsing didn't work, must be in wrong format
        }
        List<Commit> commitList = new ArrayList<>();
        List<org.gitlab4j.api.models.Commit> commits = gitlabConnectionService.getFilteredCommits(groupId, startDate, endDate, branch, commitAuthor, commitId);
        for (org.gitlab4j.api.models.Commit commit : commits) {
            Commit portfolioCommit = new Commit(commit.getId(), commit.getCommitterName(),
                    commit.getCommittedDate(), commit.getWebUrl(), commit.getMessage());
            commitList.add(portfolioCommit);
        }
        return commitList;
    }
}

