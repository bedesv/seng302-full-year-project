package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.PortfolioGroup;
import nz.ac.canterbury.seng302.portfolio.model.PortfolioGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PortfolioGroupService {

    @Autowired
    private PortfolioGroupRepository groupRepository;

    /**
     * Gets a group by its id. Creates a default group with that id if it doesn't exist
     * @param groupId The group's id from the identity provider
     * @return The group's portfolio information
     */
    public PortfolioGroup getGroupById(int groupId) {
        PortfolioGroup group;
        group = groupRepository.findByGroupId(groupId);

        // If portfolio group doesn't exist, create a new one
        if (group == null) {
            group = new PortfolioGroup(groupId);
            groupRepository.save(group);
        }
        return group;
    }

    /**
     * Gets the group's current gitlab server url. This is for the group settings page
     * @param groupId The group's id from the identity provider
     * @return The group's current gitlab server url
     */
    public String getGitlabServerUrl(int groupId) {
        PortfolioGroup group = getGroupById(groupId);
        return group.getGitlabServerUrl();
    }

    /**
     * Sets the group's gitlab server url. This is for the group settings page
     * @param groupId The group's id from the identity provider
     * @param gitlabServerUrl The group's new gitlab server url
     */
    public void setGitlabServerUrl(int groupId, String gitlabServerUrl) {
        PortfolioGroup group = getGroupById(groupId);
        group.setGitlabServerUrl(gitlabServerUrl);
        groupRepository.save(group);
    }

    /**
     * Gets the group's current gitlab project id. This is for the group settings page
     * @param groupId The group's id from the identity provider
     * @return The group's current gitlab project id
     */
    public String getGitlabProjectId(int groupId) {
        PortfolioGroup group = getGroupById(groupId);
        return group.getGitlabProjectId();
    }

    /**
     * Sets the group's gitlab project id. This is for the group settings page
     * @param groupId The group's id from the identity provider
     * @param gitlabProjectId The group's new gitlab project id
     */
    public void setGitlabProjectId(int groupId, String gitlabProjectId) {
        PortfolioGroup group = getGroupById(groupId);
        group.setGitlabProjectId(gitlabProjectId);
        groupRepository.save(group);
    }

    /**
     * Gets the group's current gitlab access token. This is for the group settings page
     * @param groupId The group's id from the identity provider
     * @return The group's current gitlab access token
     */
    public String getGitlabAccessToken(int groupId) {
        PortfolioGroup group = getGroupById(groupId);
        return group.getGitlabAccessToken();
    }

    /**
     * Sets the group's gitlab access token. This is for the group settings page
     * @param groupId The group's id from the identity provider
     * @param gitlabAccessToken The group's new gitlab access token
     */
    public void setGitlabAccessToken(int groupId, String gitlabAccessToken) {
        PortfolioGroup group = getGroupById(groupId);
        group.setGitlabAccessToken(gitlabAccessToken);
        groupRepository.save(group);
    }

    /**
     * Gets the group's current repository name. This is for the group settings page
     * @param groupId The group's id from the identity provider
     * @return The group's current repository name
     */
    public String getRepositoryName(int groupId) {
        PortfolioGroup group = getGroupById(groupId);
        return group.getRepositoryName();
    }

    /**
     * Sets the group's repository name. This is for the group settings page
     * @param groupId The group's id from the identity provider
     * @param repositoryName The group's new repository name
     */
    public void setRepositoryName(int groupId, String repositoryName) {
        PortfolioGroup group = getGroupById(groupId);
        group.setRepositoryName(repositoryName);
        groupRepository.save(group);
    }

    /**
     * Updates all of the group's repository information with the provided parameters
     * @param groupId The group's id from the identity provider
     * @param repositoryName The group's new repository name
     * @param gitlabAccessToken The group's new gitlab access token
     * @param gitlabProjectId The group's new gitlab project id
     * @param gitlabServerUrl The group's current gitlab server url
     */
    public void updateRepositoryInformation(int groupId, String repositoryName, String gitlabAccessToken, String gitlabProjectId, String gitlabServerUrl) {
        PortfolioGroup group = getGroupById(groupId);
        group.setRepositoryName(repositoryName);
        group.setGitlabAccessToken(gitlabAccessToken);
        group.setGitlabProjectId(gitlabProjectId);
        group.setGitlabServerUrl(gitlabServerUrl);
        groupRepository.save(group);
    }

    /**
     * Deletes the given portfolioGroup
     * @param groupId The group's id from the identity provider
     */
    public void deletePortfolioGroupById(int groupId) {
        groupRepository.deleteById(groupId);
    }
}
