package nz.ac.canterbury.seng302.portfolio.model.evidence;

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

    private final DateFormat formatter = new SimpleDateFormat("HH:mm:ss a MMM dd yyyy");

    public Commit(String id, String author, Date date, String link, String description) {
        this.id = id;
        this.author = author;
        this.date = date;
        this.link = link;
        this.description = description;
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
        this.author = author;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDateString() {
        return formatter.format(date);
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
