package nz.ac.canterbury.seng302.portfolio.controller.project;

import nz.ac.canterbury.seng302.portfolio.model.project.Event;
import nz.ac.canterbury.seng302.portfolio.model.project.Project;
import nz.ac.canterbury.seng302.portfolio.service.project.EventService;
import nz.ac.canterbury.seng302.portfolio.service.project.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.user.UserAccountClientService;
import nz.ac.canterbury.seng302.portfolio.util.ValidationUtil;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Controller for the edit event page
 */
@Controller
public class AddEditEventController {

    @Autowired
    UserAccountClientService userAccountClientService;
    @Autowired
    ProjectService projectService;
    @Autowired
    EventService eventService;

    private static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm";
    private static final String REDIRECT_PROJECTS = "redirect:/projects";
    private static final String REDIRECT_PROJECT_DETAILS = "redirect:/projectDetails-";
    private static final String ADD_EDIT_EVENT = "templatesProject/addEditEvent";


    /**
     * The get mapping to return the page to add/edit an event
     */
    @GetMapping("/editEvent-{eventId}-{parentProjectId}")
    public String eventForm(@AuthenticationPrincipal AuthState principal,
                            @PathVariable("parentProjectId") String parentProjectId,
                            @PathVariable("eventId") String eventId,
                            Model model) {

        //Check User is a teacher otherwise return to project page
        if (!userAccountClientService.isTeacher(principal)) {
            return REDIRECT_PROJECT_DETAILS + parentProjectId;
        }

        // Add parent project ID
        int projectId = Integer.parseInt(parentProjectId);
        Project project = projectService.getProjectById(projectId);

        Event event;
        //Check if it is existing or new event
        if (Integer.parseInt(eventId) != -1) {
            event = eventService.getEventById(Integer.parseInt(eventId));
        } else {
            //Create new event
            event = new Event();
            event.setEventName("New Event");

            Date currentDate = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            calendar.add(Calendar.DATE, 2);
            Date twoDaysTime = calendar.getTime();
            // Set the start and end dates to the current date and two days time if the event occurs within the project,
            // else set them to the project start date and two days after that.
            if(currentDate.after(project.getStartDate()) && twoDaysTime.before(project.getEndDate())) {
                event.setEventStartDate(currentDate);
                event.setEventEndDate(twoDaysTime);
            } else {
                calendar.setTime(project.getStartDate());
                calendar.add(Calendar.DATE, 2);
                Date twoDaysAfterProjectStart = calendar.getTime();
                event.setEventStartDate(project.getStartDate());
                event.setEventEndDate(twoDaysAfterProjectStart);
            }
        }
        updateModel(model, project, event);
        return ADD_EDIT_EVENT;
    }

    @PostMapping("/editEvent-{eventId}-{parentProjectId}")
    public String addEditEvent(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("parentProjectId") String projectIdString,
            @PathVariable("eventId") String eventIdString,
            @RequestParam(value="eventName") String eventName,
            @RequestParam(value="eventStartDate") String eventStart,
            @RequestParam(value="eventEndDate") String eventEnd,
            Model model) throws ParseException {
        //Check if it is a teacher making the request
        if (!userAccountClientService.isTeacher(principal)) {
            return REDIRECT_PROJECT_DETAILS + projectIdString;
        }

        // Ensure request parameters represent a valid sprint.
        // Check ids can be parsed
        int eventId;
        int projectId;

        // Convert String values of start and end date-time to timestamp
        Timestamp startDate = Timestamp.valueOf(eventStart.replace("T", " ") + ":00");
        Timestamp endDate = Timestamp.valueOf(eventEnd.replace("T", " ") + ":00");

        // Convert Timestamp values of start and end date-time to Date
        Date eventStartDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").parse(Event.dateToString(startDate));
        Date eventEndDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").parse(Event.dateToString(endDate));

        try {
            // Parse ids  from string
            eventId = Integer.parseInt(eventIdString);
            projectId = Integer.parseInt(projectIdString);
        } catch (NumberFormatException e) {
            return REDIRECT_PROJECTS;
        }
        //Check if it's an existing event
        if(eventId == -1) {
            try {
                Event newEvent = new Event(projectId, eventName, eventStartDate, eventEndDate);
                eventService.saveEvent(newEvent);
            } catch (IllegalArgumentException ignored) {
                updateModel(model, projectService.getProjectById(projectId), new Event(projectId, ValidationUtil.stripTitle(eventName), eventStartDate, eventEndDate));
                model.addAttribute("titleError", "Event name cannot contain special characters");
                return ADD_EDIT_EVENT;
            }
        } else {
            //Edit existing event
            try {
                Event existingEvent = eventService.getEventById(eventId);
                existingEvent.setEventName(eventName);
                eventService.updateEventDates(eventId, eventStartDate, eventEndDate);
                eventService.saveEvent(existingEvent);
            } catch (IllegalArgumentException ignored) {
                Event event = eventService.getEventById(eventId);
                event.setEventName(ValidationUtil.stripTitle(eventName));
                event.setEventStartDate(startDate);
                event.setEventEndDate(endDate);
                updateModel(model, projectService.getProjectById(projectId), event);
                model.addAttribute("titleError", "Event name cannot contain special characters");
                return ADD_EDIT_EVENT;
            }
        }
        return REDIRECT_PROJECT_DETAILS + projectIdString;
    }

    /**
     * The method which is used to delete an event from a project by using its id.
     * @param principal is the Authentication principal storing the current user information
     * @param parentProjectId is the id of the parent project of the event being deleted
     * @param eventId is the id of the event being deleted
     * @return the project page of the parent project
     */
    @DeleteMapping(value="/editEvent-{eventId}-{parentProjectId}")
    public String deleteProjectEventById(@AuthenticationPrincipal AuthState principal,
                                         @PathVariable("parentProjectId") String parentProjectId,
                                         @PathVariable("eventId") String eventId) {
        if (!userAccountClientService.isTeacher(principal)) {
            return REDIRECT_PROJECT_DETAILS + parentProjectId;
        }

        eventService.deleteEventById(Integer.parseInt(eventId));
        return REDIRECT_PROJECT_DETAILS + parentProjectId;
    }

    /**
     * Abstracted these additions instead of being repeated three times
     * @param model global model
     * @param project parent project id
     * @param event being updated
     */
    private void updateModel(Model model, Project project, Event event){
        model.addAttribute("projectId", project.getId());
        //Add event details to model
        model.addAttribute("eventName", event.getEventName());
        model.addAttribute("eventStartDate", Project.dateToString(event.getEventStartDate(), TIME_FORMAT));
        model.addAttribute("eventEndDate", Project.dateToString(event.getEventEndDate(), TIME_FORMAT));
        // Add event date boundaries for event to the model
        model.addAttribute("minEventStartDate", Project.dateToString(project.getStartDate(), TIME_FORMAT));
        model.addAttribute("maxEventEndDate", Project.dateToString(project.getEndDate(), TIME_FORMAT));
    }
}
