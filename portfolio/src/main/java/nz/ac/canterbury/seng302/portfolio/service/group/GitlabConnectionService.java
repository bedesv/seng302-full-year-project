package nz.ac.canterbury.seng302.portfolio.service.group;

import com.google.common.annotations.VisibleForTesting;
import nz.ac.canterbury.seng302.portfolio.model.group.GroupRepositorySettings;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.Pager;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.Member;
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
     * then fetch a list of commits from that repository. Filters the fetched commits
     * so that they meet the given filter requirements
     * @param groupId The group id to get commits from
     * @param startDate The start date of the filter range
     * @param endDate The end date of the filter range
     * @param branch A branch name in string form
     * @param authorName A gitlab user id
     * @param commitId The id of the requested commit
     * @return A list of all commits from the repository that meet the filter requirements
     */
    public List<Commit> getFilteredCommits(int groupId, Date startDate, Date endDate, String branch, String authorName, String commitId) {
        try {
            GroupRepositorySettings repositorySettings = getGroupRepositorySettings(groupId);
            GitLabApi gitLabApiConnection = getGitLabApiConnection(groupId);
            if (!Objects.equals(commitId, "")) {
              Commit commit = gitLabApiConnection.getCommitsApi().getCommit(repositorySettings.getGitlabProjectId(), commitId);
              if ((Objects.equals(authorName, "") || Objects.equals(authorName, commit.getAuthorName())) && !commit.getCommittedDate().before(startDate)
                      && !commit.getCommittedDate().after(endDate)) {
                  return List.of(commit);
              } else {
                  return new ArrayList<>();
              }
            } else {
                List<Commit> commits = gitLabApiConnection.getCommitsApi().getCommits(repositorySettings.getGitlabProjectId(), branch, startDate, endDate);
                if (!Objects.equals(authorName, "")) {
                    List<Commit> filteredCommits = new ArrayList<>();
                    for (Commit commit : commits) {
                        if ((Objects.equals(authorName, commit.getAuthorName()))) {
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
     * @param groupId The group id to get users from
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

    /**
     * Attempts to fetch a list of all branches in the repository. If it fails to connect, it
     * returns an empty list
     * @param groupId The group id to get branches from
     * @return A list of all branches in the repository
     */
    public List<Branch> getAllBranches(int groupId) {
        try {
            GitLabApi gitLabApiConnection = getGitLabApiConnection(groupId);
            GroupRepositorySettings repositorySettings = getGroupRepositorySettings(groupId);
            return gitLabApiConnection.getRepositoryApi().getBranches(repositorySettings.getGitlabProjectId());
        } catch (GitLabApiException | NoSuchFieldException exception) {
            PORTFOLIO_LOGGER.error(exception.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Checks if the repository has any commits
     * @param groupId the group id with the repo to check
     * @return true if the repository has commits, false if it doesn't or if it can't connect to the repo
     */
    public int repositoryHasCommits(int groupId) {
        try {
            GitLabApi gitLabApiConnection = getGitLabApiConnection(groupId);
            GroupRepositorySettings repositorySettings = getGroupRepositorySettings(groupId);
            Pager<Commit> commitPager = gitLabApiConnection.getCommitsApi().getCommits(repositorySettings.getGitlabProjectId(), 1);
            return commitPager.getTotalItems() != 0 ? 1 : 0;
        } catch (Exception exception) {
            PORTFOLIO_LOGGER.error(exception.getMessage());
            return -1;
        }
    }

    /**
     * Checks if the repository has 100 or more commits
     * @param groupId the group id with the repo to check
     * @return 1 if the repo has >= 100 commits, 0 if < 100 commits, -1 if it can't connect to the repository
     */
    public int repositoryHas100OrMoreCommits(int groupId) {
        try {
            GitLabApi gitLabApiConnection = getGitLabApiConnection(groupId);
            GroupRepositorySettings repositorySettings = getGroupRepositorySettings(groupId);
            Pager<Commit> commitPager = gitLabApiConnection.getCommitsApi().getCommits(repositorySettings.getGitlabProjectId(), 100);
            int totalCommits = commitPager.getTotalItems();
            // Is the number of commits if the repo has 100 or fewer commits, is -1 if the repo has more than 100 commits
            return (totalCommits >= 100 || totalCommits == -1) ? 1 : 0;
        } catch (Exception exception) {
            PORTFOLIO_LOGGER.error(exception.getMessage());
            return -1;
        }
    }
}
