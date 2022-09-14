package nz.ac.canterbury.seng302.portfolio.model.evidence;

import nz.ac.canterbury.seng302.portfolio.util.ValidationUtil;

import javax.persistence.Embeddable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Embeddable
public class Commit {

    private String id;
    private String author;
    private Date date;
    private String link;
    private String description;
    private String commitNo;

    public Commit(String id, String author, Date date, String link, String description) {
        this.id = ValidationUtil.stripTitle(id);
        setAuthor(author);
        this.date = date;
        this.link = link;
        setDescription(description);
        parseCommit(this.link);
    }

    public Commit() {
    }

    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        author = ValidationUtil.stripTitle(author);
        this.author = author;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDateString() {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        return formatter.format(date);
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
        parseCommit(this.link);
    }

    public String getCommitNo() {
        return commitNo;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Sets the description to a string, and truncates it to a maximum of 150 characters.
     * @param description A description to set.
     */
    public void setDescription(String description) {
        description = ValidationUtil.stripTitle(description);
        if (description.length() > 150) {
            description = description.substring(0, 150) + "...";
        }
        this.description = description;
    }

    /**
     * Truncates the full commit link into a new variable commitNum which is just the commit's number.
     * If it fails to find the substring 'commit/,' which should be just before th number, commitNo becomes
     * the same string as link.
     * @param link The link to parse
     * @throws IndexOutOfBoundsException Throws if the link is too small
     */
    private void parseCommit(String link) throws IndexOutOfBoundsException {
        int index = link.indexOf("commit/") + 7;
        if (index > -1) {
            this.commitNo = link.substring(index);
        } else {
            this.commitNo = link;
        }
    }
}
