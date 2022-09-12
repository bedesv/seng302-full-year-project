package nz.ac.canterbury.seng302.portfolio.service.group;

import com.google.common.annotations.VisibleForTesting;
import nz.ac.canterbury.seng302.portfolio.model.group.GroupRepositorySettings;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.Member;
import org.gitlab4j.api.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class GitlabConnectionService {
    @Autowired
    GroupRepositorySettingsService groupRepositorySettingsService;
    private static final Logger PORTFOLIO_LOGGER = LoggerFactory.getLogger("com.portfolio");

    /**
     * Fetches the given group's repository information
     * @param groupId The group id to get commits from
     * @return A GroupRepositorySettings object containing the group's repository information
     * @throws NoSuchFieldException If the given group doesn't have any repository settings
     */
    @VisibleForTesting
    protected GroupRepositorySettings getGroupRepositorySettings(int groupId) throws NoSuchFieldException {
        if (!groupRepositorySettingsService.existsByGroupId(groupId)) {
            String message = "Group " + groupId + " doesn't have any repository settings";
            PORTFOLIO_LOGGER.error(message);
            throw new NoSuchFieldException("Given group id doesn't have any repository settings");
        }
        return groupRepositorySettingsService.getGroupRepositorySettingsByGroupId(groupId);
    }

    /**
     * Connects to the given group's repository
     * @param groupId The group id to connect to
     * @return A GitLabApi connection to the given groups repository
     * @throws NoSuchFieldException If the given group doesn't have any repository settings
     */
    @VisibleForTesting
    protected GitLabApi getGitLabApiConnection(int groupId) throws NoSuchFieldException {
        GroupRepositorySettings repositorySettings = getGroupRepositorySettings(groupId);

        return new GitLabApi(repositorySettings.getGitlabServerUrl(), repositorySettings.getGitlabAccessToken());
    }

    /**
     * Attempts to create a connection to the gitlab repository in the group settings
     * then fetch a list of commits from that repository.
     * @param groupId The group id to get commits from
     * @return A list of all commits from the repository
     */
    public List<Commit> getFilteredCommits(int groupId, Date startDate, Date endDate, String branch, int userId, String commitId) {
        try {
            GroupRepositorySettings repositorySettings = getGroupRepositorySettings(groupId);
            GitLabApi gitLabApiConnection = getGitLabApiConnection(groupId);
            if (!Objects.equals(commitId, "")) {
              Commit commit = gitLabApiConnection.getCommitsApi().getCommit(repositorySettings.getGitlabProjectId(), commitId);
              if ((userId == -1 || userId == commit.getAuthor().getId()) && !commit.getCommittedDate().before(startDate)
                      && !commit.getCommittedDate().after(endDate)) {
                  return List.of(commit);
              } else {
                  return new ArrayList<>();
              }
            } else {
                List<Commit> commits = gitLabApiConnection.getCommitsApi().getCommits(repositorySettings.getGitlabProjectId(), branch, startDate, endDate);
                if (userId != -1) {
                    List<Commit> filteredCommits = new ArrayList<>();
                    for (Commit commit : commits) {
                        if ((userId == commit.getAuthor().getId())) {
                            filteredCommits.add(commit);
                        }
                    }
                    commits = filteredCommits;
                }
                return commits;
            }
        } catch  (GitLabApiException | NoSuchFieldException exception) {
            PORTFOLIO_LOGGER.error(exception.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Attempts to create a connection to the gitlab repository in the group settings
     * then fetch a list of commits from that repository.
     * @param groupId The group id to get commits from
     * @return A list of all commits from the repository
     * @throws GitLabApiException If it can't connect to the project
     * @throws NoSuchFieldException If the given group doesn't have any repository settings
     */
    public List<Commit> getAllCommits(int groupId) throws GitLabApiException, NoSuchFieldException {
        GroupRepositorySettings repositorySettings = getGroupRepositorySettings(groupId);
        GitLabApi gitLabApiConnection = getGitLabApiConnection(groupId);
        return gitLabApiConnection.getCommitsApi().getCommits(repositorySettings.getGitlabProjectId());
    }

    /**
     * Attempts to fetch a list of all users in the repository. If it fails to connect, it
     * returns an empty list
     * @param groupId The group id to get commits from
     * @return A list of all users in the repository
     */
    public List<Member> getAllMembers(int groupId) {
        try {
            GitLabApi gitLabApiConnection = getGitLabApiConnection(groupId);
            GroupRepositorySettings repositorySettings = getGroupRepositorySettings(groupId);
            return gitLabApiConnection.getProjectApi().getAllMembers(repositorySettings.getGitlabProjectId());
        } catch (GitLabApiException | NoSuchFieldException exception) {
            PORTFOLIO_LOGGER.error(exception.getMessage());
            return new ArrayList<>();
        }
    }
}
