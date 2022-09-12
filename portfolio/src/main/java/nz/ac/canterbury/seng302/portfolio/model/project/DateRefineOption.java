package nz.ac.canterbury.seng302.portfolio.model.project;

import java.util.Date;

public class DateRefineOption {

    private final String title;
    private final Date startDate;
    private final Date endDate;

    public DateRefineOption (String title, Date startDate, Date endDate){
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getTitle(){ return title;}

    public Date getStartDate() {return startDate;}

    public Date getEndDate() {return endDate;}

}
