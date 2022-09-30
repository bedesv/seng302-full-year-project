package nz.ac.canterbury.seng302.portfolio.service.user;

import nz.ac.canterbury.seng302.portfolio.model.user.PortfolioUser;
import nz.ac.canterbury.seng302.portfolio.model.project.Project;
import nz.ac.canterbury.seng302.portfolio.repository.user.PortfolioUserRepository;
import nz.ac.canterbury.seng302.portfolio.repository.project.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.service.project.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortfolioUserService {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private PortfolioUserRepository repository;

    @Autowired
    private ProjectRepository projectRepository;

    private static final Logger PORTFOLIO_LOGGER = LoggerFactory.getLogger("com.portfolio");
    private static final String USER = "User ";

    /**
     * Gets a user by their id. Creates a default user with that id if none exists.
     * @param id The user's id from the identity provider
     * @return The user's portfolio information
     */
    public PortfolioUser getUserById(int id) {
        PortfolioUser user = repository.findByUserId(id);
        if (user != null) {
            return user;
        } else {
            // Ascending by name is the default user list sort
            PortfolioUser newUser = new PortfolioUser(id, "name", true);
            resetCurrentProject(newUser);
            repository.save(newUser);
            return newUser;
        }
    }

    /**
     * Reset the current project of a user to the first project in the application, or make a new project if none exist.
     * Should be used when creating a portfolio user, or if that user's current project is null.
     * @param user The user to reset the current project of
     */
    private void resetCurrentProject(PortfolioUser user) {
        List<Project> projects = projectService.getAllProjects();
        if (projects.isEmpty()) {
            Project defaultProject = new Project();
            projectService.saveProject(defaultProject);
            projects = projectService.getAllProjects();
        }
        user.setCurrentProject(projects.get(0).getId());
    }

    /**
     * Gets a user's user list sort type. This is for the user list page.
     * Creates a default user with that id if none exists.
     * @param id The user's id from the identity provider
     * @return The user's current user list sort type
     */
    public String getUserListSortType(int id) {
        PortfolioUser user = getUserById(id);
        return user.getUserListSortType();
    }

    /**
     * Sets a user's user list sort type. This is for the user list page.
     * Creates a default user with that id if none exists.
     * @param id The user's id from the identity provider
     * @param userListSortType The sort type to change to
     */
    public void setUserListSortType(int id, String userListSortType) {
        PortfolioUser user = getUserById(id);
        user.setUserListSortType(userListSortType);
        repository.save(user);

        // Replaces pattern-breaking characters
        String parsedSortType = userListSortType.replaceAll("[\n\r\t]", "_");
        String message = USER + id + " sort type changed to " + parsedSortType;
        PORTFOLIO_LOGGER.info(message);
    }

    /**
     * Gets whether a user's user list sort is ascending. This is for the user list page.
     * Creates a default user with that id if none exists.
     * @param id The user's id from the identity provider
     * @return Whether a user's user list sort is ascending
     */
    public boolean isUserListSortAscending(int id) {
        PortfolioUser user = getUserById(id);
        return user.isUserListSortAscending();
    }

    /**
     * Sets whether a user's user list sort is ascending. This is for the user list page.
     * Creates a default user with that id if none exists.
     * @param id The user's id from the identity provider
     * @param userListSortIsAscending Whether a user's user list sort should be ascending
     */
    public void setUserListSortAscending(int id, boolean userListSortIsAscending) {
        PortfolioUser user = getUserById(id);
        user.setUserListSortAscending(userListSortIsAscending);
        repository.save(user);
        String message = USER + id + " sort order is ascending changed to " + userListSortIsAscending;
        PORTFOLIO_LOGGER.info(message);
    }

    /**
     * Get authenticated user's current project
     * @param userId of authenticated user
     * @return current selected project else first in project repo
     */
    public Project getCurrentProject(int userId) {
        PortfolioUser portfolioUser = getUserById(userId);
        int id = portfolioUser.getCurrentProject();
        Project project = projectRepository.findById(id);
        if (project == null) {
            resetCurrentProject(portfolioUser);
            repository.save(portfolioUser);
            return projectRepository.findById(portfolioUser.getCurrentProject());
        } else {
            return project;
        }
    }

    /**
     * Set Portfolio Users currently selected project
     * @param userId of user selecting a project
     * @param projectId of project being selected
     */
    public void setProject(int userId, int projectId) {
        PortfolioUser portfolioUser = getUserById(userId);
        portfolioUser.setCurrentProject(projectId);
        repository.save(portfolioUser);
        String message = USER + userId + " current project changed to project " + projectId;
        PORTFOLIO_LOGGER.info(message);
    }

    /**
     * Get a list of skills from a Portfolio User
     * @param userId unique id of User from idp, and PortfolioUser
     * @return list of their skills
     */
    public List<String> getPortfolioUserSkills(int userId){
        PortfolioUser portfolioUser = getUserById(userId);
        return portfolioUser.getSkills();
    }

    /**
     * Iterate through the list of skills, and add to the given user
     * @param userId unique id of User from idp, and PortfolioUser
     * @param skills list of skills being added
     */
    public void addPortfolioUserSkills(int userId, List<String> skills){
        try {
            PortfolioUser portfolioUser = getUserById(userId);
            portfolioUser.addSkills(skills);
            repository.save(portfolioUser);
            String message = USER + userId + " given skills " + skills;
            PORTFOLIO_LOGGER.info(message);
        } catch (Exception e) {
            //should never reach as getUserById creates user if one doesn't exist
        }
    }

}
