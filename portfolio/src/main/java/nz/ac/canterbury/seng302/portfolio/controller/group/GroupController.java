package nz.ac.canterbury.seng302.portfolio.controller.group;

import nz.ac.canterbury.seng302.portfolio.model.evidence.PortfolioEvidence;
import nz.ac.canterbury.seng302.portfolio.model.group.Group;
import nz.ac.canterbury.seng302.portfolio.model.group.GroupRepositorySettings;
import nz.ac.canterbury.seng302.portfolio.model.project.Project;
import nz.ac.canterbury.seng302.portfolio.service.evidence.EvidenceService;
import nz.ac.canterbury.seng302.portfolio.service.group.*;
import nz.ac.canterbury.seng302.portfolio.service.project.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.user.UserAccountClientService;
import nz.ac.canterbury.seng302.portfolio.util.ValidationUtil;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.GroupDetailsResponse;
import org.gitlab4j.api.GitLabApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class GroupController {
    private static final String GROUP_PAGE = "templatesGroup/group";
    private static final String GROUP_REPOSITORY = "fragmentsGroup/groupRepositorySettings";

    @Autowired
    private UserAccountClientService userAccountClientService;

    @Autowired
    private GroupsClientService groupsClientService;

    @Autowired
    private GroupRepositorySettingsService groupRepositorySettingsService;
    @Autowired
    private GitlabConnectionService gitlabConnectionService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private PortfolioGroupService portfolioGroupService;
    @Autowired
    private GroupChartDataService groupChartDataService;
    @Autowired
    private EvidenceService evidenceService;

    private static final int GROUP_HOME_EVIDENCE_LIMIT = 20;
    private static final Logger PORTFOLIO_LOGGER = LoggerFactory.getLogger("com.portfolio");

    /**
     * Get mapping to fetch group settings page
     * @param principal Authentication principal storing current user information
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return The  group settings html page
     */
    @GetMapping("/group-{id}")
    public String groups(@AuthenticationPrincipal AuthState principal, Model model, @PathVariable String id){
        int userId = userAccountClientService.getUserId(principal);
        int groupId = Integer.parseInt(id);
        GroupDetailsResponse response = groupsClientService.getGroupDetailsById(groupId);
        if (response.getGroupId() == 0) {
            return "redirect:/groups";
        }
        Group group = new Group(response);
        Project project = projectService.getProjectById((portfolioGroupService.getPortfolioGroupByGroupId(group.getGroupId())).getParentProjectId());
        List<PortfolioEvidence> evidenceList = evidenceService.getEvidenceForPortfolioByGroup(group, project.getId(), GROUP_HOME_EVIDENCE_LIMIT);
        model.addAttribute("evidenceList", evidenceList);
        model.addAttribute("skillsList", evidenceService.getAllGroupsSkills(group, project.getId()));
        model.addAttribute("group", group);
        model.addAttribute("userInGroup", groupsClientService.userInGroup(group.getGroupId(), userId));
        model.addAttribute("graphStartDate", project.getStartDate());
        model.addAttribute("graphEndDate", project.getEndDate());
        model.addAttribute("timeRange", "day");
        model.addAttribute("evidenceList", evidenceList);
        model.addAttribute("pageUser", userAccountClientService.getUserAccountByPrincipal(principal));
        groupChartDataService.setDateRefiningOptions(model, project);
        return GROUP_PAGE;
    }

    /**
     * Get mapping to fetch an updated copy of the group repository information
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @param id The group id
     * @return A html fragment that contains the updated repository information
     */
    @GetMapping("/group-{id}-repository")
    public String groupRepository(Model model, @PathVariable String id, @RequestParam("firstLoad") boolean firstLoad) {
        GroupRepositorySettings groupRepositorySettings = groupRepositorySettingsService.getGroupRepositorySettingsByGroupId(Integer.parseInt(id));

        int moreThan100Commits = gitlabConnectionService.repositoryHas100OrMoreCommits(Integer.parseInt(id));
        int numCommits = 0;
        if (moreThan100Commits == 0) {
            try {
                numCommits = gitlabConnectionService.getAllCommits(Integer.parseInt(id)).size();
            } catch (GitLabApiException | NoSuchFieldException e) {
                PORTFOLIO_LOGGER.error(e.getMessage());
            }
        }
        model.addAttribute("firstLoad", firstLoad);
        model.addAttribute("changesSaved", false);
        model.addAttribute("moreThan100Commits", moreThan100Commits);
        model.addAttribute("numCommits", numCommits);
        model.addAttribute("groupRepositorySettings", groupRepositorySettings);
        return GROUP_REPOSITORY;
    }

    /**
     * A post mapping to update the given groups repository
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @param repositoryName The new repository name
     * @param gitlabAccessToken The new repository api key
     * @param gitlabProjectId The new repository id
     * @param gitlabServerUrl The new repository server URL
     * @param id The group id
     * @return A html fragment that contains the updated repository information
     */
    @PostMapping("/group-{id}-repository")
    public String updateGroupRepository(Model model,
                                        @RequestParam("repositoryName") String repositoryName,
                                        @RequestParam("gitlabAccessToken") String gitlabAccessToken,
                                        @RequestParam("gitlabProjectId") String gitlabProjectId,
                                        @RequestParam("gitlabServerUrl") String gitlabServerUrl,
                                        @PathVariable String id) {
        // Update the group repository information
        int groupId = Integer.parseInt(id);
        groupRepositorySettingsService.updateRepositoryInformation(groupId, ValidationUtil.stripTitle(repositoryName), ValidationUtil.stripTitle(gitlabAccessToken), gitlabProjectId, ValidationUtil.stripTitle(gitlabServerUrl));

        // Return the updated repository information
        GroupRepositorySettings groupRepositorySettings = groupRepositorySettingsService.getGroupRepositorySettingsByGroupId(groupId);
        int moreThan100Commits = gitlabConnectionService.repositoryHas100OrMoreCommits(groupId);
        int numCommits = 0;
        if (moreThan100Commits == 0) {
            try {
                numCommits = gitlabConnectionService.getAllCommits(groupId).size();
            } catch (GitLabApiException | NoSuchFieldException e) {
                PORTFOLIO_LOGGER.error(e.getMessage());
            }
        }
        model.addAttribute("firstLoad", false);
        model.addAttribute("changesSaved", true);
        model.addAttribute("moreThan100Commits", moreThan100Commits);
        model.addAttribute("numCommits", numCommits);
        model.addAttribute("groupRepositorySettings", groupRepositorySettings);
        return GROUP_REPOSITORY;
    }

    /**
     * Fetches the GROUP_HOME_EVIDENCE_LIMIT most recent pieces of evidence for the given group with
     * the given skill
     * @param model The model to add the data to
     * @param skill The skill to filter by. Is '#no_skill' if wanting to get evidence with no skill
     * @param id The id of the group to fetch evidence for
     * @return The evidence page with the new evidence filled in
     */
    @GetMapping("/group-{id}-evidence-skill")
    public String getGroupEvidenceFilteredBySkill(Model model,
                                                  @RequestParam("skill") String skill,
                                                  @PathVariable String id) {
        int groupId = Integer.parseInt(id);
        GroupDetailsResponse response = groupsClientService.getGroupDetailsById(groupId);
        if (response.getGroupId() == 0) {
            return "redirect:/groups";
        }
        Group group = new Group(response);
        Project project = projectService.getProjectById((portfolioGroupService.getPortfolioGroupByGroupId(group.getGroupId())).getParentProjectId());
        model.addAttribute("evidenceList", evidenceService.getEvidenceForPortfolioByGroupFilterBySkill(group, project.getId(), skill, GROUP_HOME_EVIDENCE_LIMIT));
        return "fragments/evidence";
    }

    /**
     * Fetches the GROUP_HOME_EVIDENCE_LIMIT most recent pieces of evidence for the given group
     * @param model The model to add the data to
     * @param id The id of the group to fetch evidence for
     * @return The evidence page with the new evidence filled in
     */
    @GetMapping("/group-{id}-evidence")
    public String getGroupEvidence(Model model,
                                   @PathVariable String id) {
        int groupId = Integer.parseInt(id);
        GroupDetailsResponse response = groupsClientService.getGroupDetailsById(groupId);
        if (response.getGroupId() == 0) {
            return "redirect:/groups";
        }
        Group group = new Group(response);
        Project project = projectService.getProjectById((portfolioGroupService.getPortfolioGroupByGroupId(group.getGroupId())).getParentProjectId());
        model.addAttribute("evidenceList", evidenceService.getEvidenceForPortfolioByGroup(group, project.getId(), GROUP_HOME_EVIDENCE_LIMIT));
        return "fragments/evidence";
    }


}
