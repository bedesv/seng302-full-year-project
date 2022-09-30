package nz.ac.canterbury.seng302.portfolio.model.evidence;

import nz.ac.canterbury.seng302.portfolio.model.user.User;

import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Class for displaying evidence on the portfolio frontend.
 * Is identical to the evidence class but userid fields have been replaced with user fields.
 * The model cannot store users soo something like this is needed.
 */
public class PortfolioEvidence {

    private final int id;
    private final int ownerId; // ID of the user who owns this evidence piece
    private final int projectId; // ID of the project this evidence relates to
    private final String title;
    private final String description;
    private final List<Categories> categories;
    private final Date date;
    private final List<WebLink> webLinks;
    private final List<String> skills; //skills related to this piece of evidence
    private final List<User> linkedUsers;
    private List<Commit> commits;
    private List<Integer> likes;

    /**
     * Creates a piece of evidence for display in the portfolio. This is identical to a regular piece of evidence,
     * but requires a list of users instead of a list of user ids.
     * @param evidence A piece of evidence
     * @param userList A list of users corresponding to the user ids from the piece of evidence
     */
    public PortfolioEvidence(Evidence evidence, List<User> userList) {
        this.id = evidence.getId();
        this.ownerId = evidence.getOwnerId();
        this.projectId = evidence.getProjectId();
        this.title = evidence.getTitle();
        this.description = evidence.getDescription();
        this.categories = evidence.getCategories();
        this.date = evidence.getDate();
        this.webLinks = evidence.getWebLinks();
        this.skills = evidence.getSkills();
        this.linkedUsers = userList;
        this.commits = evidence.getCommits();
        this.likes = evidence.getLikes();
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getProjectId() {
        return projectId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Date getDate() {
        return date;
    }

    public String getDateString() {
        return new SimpleDateFormat("dd-MM-yyyy").format(date);
    }

    public List<WebLink> getWebLinks() {
        return webLinks;
    }

    public List<String> getSkills() {return skills;}

    public List<User> getLinkedUsers() {
        return linkedUsers;
    }

    public int getNumberWeblinks() { return webLinks.size(); }

    public List<Categories> getCategories() {
        List<Categories> sortedCategories = new ArrayList<>(categories);
        Collections.sort(sortedCategories);
        return sortedCategories;
    }

    public int getNumberCommits() {
        return commits.size();
    }

    public List<Commit> getCommits() {
        return commits;
    }

    /**
     * Returns true if the user has liked the piece of evidence
     */
    public boolean hasLiked(int userId) {
        return likes.contains(userId);
    }

    public int getNumberOfLikes() {
        return likes.size();
    }

}