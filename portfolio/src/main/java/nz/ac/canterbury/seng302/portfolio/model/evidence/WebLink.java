package nz.ac.canterbury.seng302.portfolio.model.evidence;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Embeddable class to allow weblinks to be stored in evidence without creating a weblinks' entity.
 * Easiest way to store name and weblink together.
 */
@Embeddable
public class WebLink {
    String name;
    boolean safe;
    @Column(columnDefinition = "LONGTEXT")
    String link;

    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";

    public WebLink(String webLink, String name) {
        this.name = name;
        if (webLink.matches("http://.*")) {
            this.link = webLink.replaceFirst(HTTP, "");
            this.safe = false;
        } else if (webLink.matches("https://.*")) {
            this.link = webLink.replaceFirst(HTTPS, "");
            this.safe = true;
        }

    }

    public WebLink(String webLink) {
        if (webLink.matches("http://.*")) {
            this.link = webLink.replaceFirst(HTTP, "");
            this.safe = false;
        } else if (webLink.matches("https://.*")) {
            this.link = webLink.replaceFirst(HTTPS, "");
            this.safe = true;
        }
    }

    public WebLink() {
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }

    public String getFullLink() {
        return this.isSafe() ? HTTPS + this.link : HTTP + this.link;
    }

    public boolean isSafe() {
        return this.safe;
    }
}
