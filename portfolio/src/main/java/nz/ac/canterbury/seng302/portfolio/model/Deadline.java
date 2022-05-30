package nz.ac.canterbury.seng302.portfolio.model;

import javax.persistence.*;
import java.util.Date;

@Entity // this is an entity, assumed to be in a table called Deadline
@Table(name="DEADLINE")
public class Deadline {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int deadlineId;
    private int deadlineParentProjectId;
    private String deadlineName;
    private Date deadlineDate;
    
    public Deadline() {}
    
    public Deadline(int deadlineParentProjectId, String deadlineName, Date deadlineDate) {
        this.deadlineParentProjectId = deadlineParentProjectId;
        this.deadlineName = deadlineName;
        this.deadlineDate = deadlineDate;
    }
    
    @Override
    public String toString() {
        return String.format(
                "Deadline[deadlineId=%d, deadlineParentProjectId='%d', deadlineName='%s', deadlineEndDate='%s']",
                deadlineId, deadlineParentProjectId, deadlineName, deadlineDate);
    }

    /* Getters/Setters */

    public int getDeadlineId() {
        return deadlineId;
    }

    public int getDeadlineParentProjectId() {
        return deadlineParentProjectId;
    }

    public String getDeadlineName() {
        return deadlineName;
    }

    public void setDeadlineName(String deadlineName) {
        this.deadlineName = deadlineName;
    }

    public Date getDeadlineDate() {
        return deadlineDate;
    }

    public void setDeadlineDate(Date deadlineEndDate) {
        this.deadlineDate = deadlineEndDate;
    }
}
