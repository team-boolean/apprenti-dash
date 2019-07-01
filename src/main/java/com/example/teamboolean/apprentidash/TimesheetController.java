package com.example.teamboolean.apprentidash;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.security.Principal;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;

@Controller
public class TimesheetController {
    //US Zone ID
    private final static ZoneId USZONE = ZoneId.of("America/Los_Angeles");
    //first Day of the week
    private DayOfWeek firstDay;
    //Day list based from date range
    private List<Day> dateRange;
    //total hours worked
    private double totalHours;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DayRepository dayRepository;

    /********************************* The controller methods to handle our Punch In page **************************************************************/
  //route to handle when a user first comes to the punch in page
    @GetMapping("/recordHour")
    public String recordHour(Model m, Principal p){
        //Sets the necessary variables for the nav bar
        loggedInStatusHelper(m, p);
        m.addAttribute("currentPage", "clock_in");
        //Sets status for knowing which button to show
        LocalDateTime now = LocalDateTime.now(USZONE);
        AppUser currentUser = userRepository.findByUsername(p.getName());
        m.addAttribute("workStatus", buttonRenderHelper(currentUser));
        m.addAttribute("todayDate", now);
        return "recordHour";
    }

    //Route to handle our clock in button
    @PostMapping(value="/recordHour", params="name=value")
    public String clockInSave(Principal p, Model m) {

        AppUser currentUser = userRepository.findByUsername(p.getName());
        LocalDateTime now = LocalDateTime.now(USZONE);

        //check what day instance variable needs to be updated based on the sequence of clockin-lunchin-lunchout-clockout
        if(buttonRenderHelper(currentUser).equals("clockIn")) {
            currentUser.getCurrentday().setClockIn(now);
        }else if(buttonRenderHelper(currentUser).equals(("lunchIn"))) {
            currentUser.getCurrentday().setLunchStart(now);
        }else if(buttonRenderHelper(currentUser).equals("lunchOut")) {
            currentUser.getCurrentday().setLunchEnd(now);
        }else if(buttonRenderHelper(currentUser).equals("clockOut")){
            currentUser.getCurrentday().setClockOut(now);
        }

        //set the day instance user to the current user
        currentUser.getCurrentday().setUser(currentUser);
        dayRepository.save(currentUser.getCurrentday());
        m.addAttribute("workStatus", buttonRenderHelper(currentUser));
        return "redirect:/recordHour";
    }


    //route to handle when a user wants to add an additional day to their record
    @GetMapping ("/additionalDayRecord")
    public RedirectView makeDay(Principal p){
        AppUser currentUser = userRepository.findByUsername(p.getName());
        currentUser.setCurrentday(null);
        userRepository.save(currentUser);
        return new RedirectView("/recordHour");
    }

/******************************** End of the controller for handle Punch In page ********************************************************************/


/******************************** Summary Route ******************************************************************************/
    @GetMapping("/summary")
    public String getSummary(Principal p, Model m, String fromDate, String toDate){
        //Sets the necessary variables for the nav bar
        loggedInStatusHelper(m, p);
        m.addAttribute("currentPage", "summary");

        //Retrieve info of logged in user
        AppUser currentUser = userRepository.findByUsername(p.getName());

        //Add to the model for display
        m.addAttribute("user", currentUser);

        //Get associated days of the user
        List<Day> userDays = currentUser.days;

        //initialize list
        dateRange = new ArrayList<>();

        //Get first day of the current week
        LocalDate from = getFirstDay();
        //Get last day of current week
        LocalDate to = getLastDay();

        //Check if input dates are not null, if not convert into local date
        if (fromDate != null){
            from = LocalDate.parse(fromDate);
        }

        if (toDate != null){
            to = LocalDate.parse(toDate);
        }

        //Current work hours so far
        totalHours = 0.00;
        // retrieves the days based from date range and compute the
        // total working hours
        for (Day curDay: userDays){
            LocalDate local = curDay.clockIn.toLocalDate();

            if (local.compareTo(from) >= 0 && local.compareTo(to)<= 0){
                dateRange.add(curDay);
                totalHours += curDay.calculateDailyHours();
            }
        }

        //Sort the list by clock-in dates
        sortDateList();

        //Add to model for display in summary.html
        m.addAttribute("fromDate", from);
        m.addAttribute("toDate", to);
        m.addAttribute("days", dateRange);
        m.addAttribute("totalHours", totalHours);
        return "summary";
    }


    /************************************ Controller to handle the Edit page ***************************************************************************/
    @GetMapping("/edit/{dayId}")
    public String getEdit(@PathVariable long dayId, Model m, Principal p) {
        //Sets the necessary variables for the nav bar
        loggedInStatusHelper(m, p);
        m.addAttribute("currentPage", "clock_in");

        Day currentDay = dayRepository.findById(dayId).get();
        AppUser currentUser = userRepository.findByUsername(p.getName());
        //check if the day the user is trying to modify belongs to the user
        if(!currentUser.days.contains(currentDay))
            return "error";
        else{
            m.addAttribute("currentDay", currentDay);
            return "edit";
        }
    }

    @PostMapping("/edit")
    public String postEdit(long dayId,String clockIn, String clockOut, String lunchStart, String lunchEnd){
        Day currentDay = dayRepository.findById(dayId).get();
        LocalTime clockInLocalTime = LocalTime.parse(clockIn);
        //update the clockIn to column to the newly edited hours and minutes
        currentDay.setClockIn(currentDay.getClockIn().withHour(clockInLocalTime.getHour()).withMinute(clockInLocalTime.getMinute()));

        //code to check if the user make any modifications to the lunch start date field
        if(!(lunchStart.equals(""))){
            LocalTime lunchStartLocalTime = LocalTime.parse(lunchStart);
            //check if the user never started a lunch in before trying to edit his/her lunch in
            if(currentDay.getLunchStart() == null){
                //set the lunch start date to the clock in date
                currentDay.setLunchStart(currentDay.getClockIn());
                //overwrite the hours and minutes of the lunch start to match with the modifications the user made
                currentDay.setLunchStart(currentDay.getLunchStart().withHour(lunchStartLocalTime.getHour()).withMinute(lunchStartLocalTime.getMinute()));
            }else{
                //if the user already clicked on lunch in, just update the newly modified hours and minutes
                currentDay.setLunchStart(currentDay.getLunchStart().withHour(lunchStartLocalTime.getHour()).withMinute(lunchStartLocalTime.getMinute()));
            }
        }

        //code to check if the user make any modifications to the lunch end date field
        if(!(lunchEnd.equals(""))){
            LocalTime lunchEndLocalTime = LocalTime.parse(lunchEnd);
            //check if the user never started a lunch out before trying to edit his/her lunch out
            if(currentDay.getLunchEnd() == null){
                //set the lunch end date to the clock in date
                currentDay.setLunchEnd(currentDay.getClockIn());
                //overwrite the hours and minutes of the lunch end to match with the modifications the user made
                currentDay.setLunchEnd(currentDay.getLunchEnd().withHour(lunchEndLocalTime.getHour()).withMinute(lunchEndLocalTime.getMinute()));
            }else{
                //if the user already clicked on lunch out, just update the newly modified hours and minutes
                currentDay.setLunchEnd(currentDay.getLunchEnd().withHour(lunchEndLocalTime.getHour()).withMinute(lunchEndLocalTime.getMinute()));
            }
        }

        //code to check if the user make any modifications to the clock out date field
        if(!(clockOut.equals(""))){
            LocalTime clockOutLocalTime = LocalTime.parse(clockOut);
            //check if the user never clocked out before trying to edit his/her clock out
            if(currentDay.getClockOut() == null){
                //set the clock out date to the clock in date
                currentDay.setClockOut(currentDay.getClockIn());
                //overwrite the hours and minutes of the clock out to match with the modifications the user made
                currentDay.setClockOut(currentDay.getClockOut().withHour(clockOutLocalTime.getHour()).withMinute(clockOutLocalTime.getMinute()));
            }else{
                //if the user already clicked on clock out, just update the newly modified hours and minutes
                currentDay.setClockOut(currentDay.getClockOut().withHour(clockOutLocalTime.getHour()).withMinute(clockOutLocalTime.getMinute()));
            }
        }
        dayRepository.save(currentDay);
        return "redirect:/summary";
    }

    //DeleteMapping to clear Id for day in DB
    @DeleteMapping("/delete/{dayId}")
    public String deleteDay(@PathVariable long dayId, Principal p){
        Day currentDay = dayRepository.findById(dayId).get();
        AppUser currentUser = userRepository.findByUsername(p.getName());

        //check if the day the user wants to delete belongs to the user
        if(!currentUser.days.contains(currentDay))
            return "error";
        else{
            //check if the day the user wants to delete is a day the user has clocked in but not clocked out for
            if(currentUser.getCurrentday() == currentDay){
                //reallocate the users currentday instance reference to null, before deleting the day
                currentUser.setCurrentday(null);
                userRepository.save(currentUser);
                dayRepository.delete(currentDay);
            }else{
                dayRepository.delete(currentDay);
            }
            return "redirect:/summary";
        }
    }

    /************************************ End of Controller to handle the Edit page ***************************************************************************/


    /***************************** CSV CONTROLLER ***************************/

    @GetMapping("/timesheet")
    public void exportCSV(HttpServletResponse response) throws Exception {

        //set file name and content type
        String filename = "timesheet.csv";

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\"");

        //Write to file and download
        PrintWriter csvWriter = response.getWriter();

        String header = "Day,Date,Time In,Time Out,Lunch,Daily Hours";
        csvWriter.println(header);

        for(Day curDay: dateRange){

            csvWriter.println(curDay.toString());
        }

        csvWriter.println(",,,,Total Hours:," + totalHours);
        csvWriter.close();

    }


    /******************************** All the helper function ************************************/

    //helper function to handle the punch in page. It checks which day instance variable hasnt been clicked yet, and returns that to the view to
    // display a button for it
    public String buttonRenderHelper(AppUser currentUser ){
        if(currentUser.getCurrentday() == null) {
            Day day = new Day();
            currentUser.setCurrentday(day);
        }
        if(currentUser.getCurrentday().getClockIn() == null)
            return "clockIn";
        else if(currentUser.getCurrentday().getLunchStart() == null)
            return "lunchIn";
        else if(currentUser.getCurrentday().getLunchEnd() == null)
            return "lunchOut";
        else if(currentUser.getCurrentday().getClockOut() == null)
            return "clockOut";
        else
            return "notNewDay";
    }


    //Checks if the user is logged in and sets the model attributes accordingly per the navbar requirements
    protected void loggedInStatusHelper(Model m, Principal p){

        //Navbar required variables for knowing if user is logged in and their name for display
        boolean isLoggedIn;
        String currentUserFirstName;

        //Check if the user is logged in and sets the variables
        if(p == null){
            isLoggedIn = false;
            currentUserFirstName = "Visitor";
        }else {
            isLoggedIn = true;
            currentUserFirstName = userRepository.findByUsername(p.getName()).getFirstName();
        }

        //add the attributes to the passed in model
        m.addAttribute("isLoggedIn", isLoggedIn);
        m.addAttribute("userFirstName", currentUserFirstName);
    }

    //Helper function to get the first day
    //Reference: https://stackoverflow.com/questions/22890644/get-current-week-start-and-end-date-in-java-monday-to-sunday
    private LocalDate getFirstDay(){
        firstDay = WeekFields.of(Locale.US).getFirstDayOfWeek();
        return LocalDate.now(USZONE).with(TemporalAdjusters.previousOrSame(firstDay));
    }

    //Helper function to get the last day
    //Reference: https://stackoverflow.com/questions/22890644/get-current-week-start-and-end-date-in-java-monday-to-sunday
    private LocalDate getLastDay(){
        DayOfWeek lastDay = DayOfWeek.of(((firstDay.getValue() + 5) % DayOfWeek.values().length) + 1);
        return LocalDate.now(USZONE).with(TemporalAdjusters.nextOrSame(lastDay));

    }

    //Sort dates from earliest to latest
    //Ref: http://java-buddy.blogspot.com/2013/01/sort-list-of-date.html
    private void sortDateList(){
        Collections.sort(dateRange, new Comparator<Day>(){

            @Override
            public int compare(Day o1, Day o2) {
                return o1.clockIn.compareTo(o2.clockIn);
            }
        });
    }


}
