package com.example.teamboolean.apprentidash;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.constraints.Null;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;

import java.security.Principal;

import java.time.format.FormatStyle;
import java.util.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.time.LocalDate.now;



@Controller
public class ApprentiDashController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    DayRepository dayRepository;

    Day currentDay = new Day();

    @GetMapping("/")
    public String getHome(Model m, Principal p){
        //Sets the necessary variables for the nav bar
        loggedInStatusHelper(m, p);
        return "home";
    }

    @GetMapping("/login")
    public String getLogin(Model m, Principal p){
        loggedInStatusHelper(m, p);
        return "login";
    }

    @GetMapping("/signup")
    public String startSignUp(Model m, Principal p){
        loggedInStatusHelper(m, p);
        return "signup";
    }

    @PostMapping("/signup")
    public String addUser(String username, String password, String firstName, String lastName, String managerName){
        if (!checkUserName(username)) {
            AppUser newUser = new AppUser(username, passwordEncoder.encode(password), firstName, lastName, managerName);
            userRepository.save(newUser);
            Authentication authentication = new UsernamePasswordAuthenticationToken(newUser, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return "redirect:/";
        }else {
            return "duplicateUsername";
        }
    }

    /********************************* The controller methods to handle our Punch In page **************************************************************/
    @GetMapping("/recordHour")
    public String recordHour(Model m){
        String todayDate =  java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC));
        m.addAttribute("workStatus", buttonRenderHelper());
        m.addAttribute("todayDate", todayDate);
        return "recordHour";
    }

//Route to handle our clock in button
    @PostMapping(value="/recordHour", params="name=value")
    public String clockInSave(Principal p, Model m) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        boolean endOfDay = false;

        if(buttonRenderHelper().equals("clockIn")) {
            currentDay.setClockIn(now);
        }else if(buttonRenderHelper().equals(("lunchIn"))) {
            currentDay.setLunchStart(now);
        }else if(buttonRenderHelper().equals("lunchOut")) {
            currentDay.setLunchEnd(now);
        }else if(buttonRenderHelper().equals("clockOut")){
            currentDay.setClockOut(now);
            endOfDay = true;
        }
        currentDay.setUser(userRepository.findByUsername(p.getName()));
        dayRepository.save(currentDay);
        if(endOfDay){
            currentDay = new Day();
        }
        m.addAttribute("workStatus", buttonRenderHelper());
        return "redirect:/recordHour";
    }

    public String buttonRenderHelper(){
        if(currentDay.getClockIn() == null)
            return "clockIn";
        else if(currentDay.getLunchStart() == null)
            return "lunchIn";
        else if(currentDay.getLunchEnd() == null)
            return "lunchOut";
        else if(currentDay.getClockOut() == null)
            return "clockOut";
        return null;
    }
/******************************** End of the controller for handle Punch In page ********************************************************************/

    @GetMapping("/summary")
    public String getSummary(Principal p, Model m, String fromDate, String toDate){
        loggedInStatusHelper(m, p);
        AppUser currentUser = userRepository.findByUsername(p.getName());
        m.addAttribute("localDate", now());
        m.addAttribute("user", currentUser);

        // retrieve by date from the DB.
        List<Day> userDays = currentUser.days;
        List<Day> dateRange = new ArrayList<>();
        // TODO: initialize based from the current's date' week
        LocalDate from = LocalDate.now();
        LocalDate to = LocalDate.now();


        if (fromDate != null && toDate != null){

            from = LocalDate.parse(fromDate);
            to = LocalDate.parse(toDate);
        }

        double totalHours = 0.0;
        // retrieves the days associated to the logged in user
        for (Day curDay: userDays){
            LocalDate local = curDay.clockIn.toLocalDate();

            if (local.compareTo(from) >= 0 && local.compareTo(to)<= 0){
                System.out.println(local);
                dateRange.add(curDay);
                totalHours += curDay.calculateDailyHours();
            }
        }
        m.addAttribute("days", dateRange);
        m.addAttribute("totalHours", totalHours);
        return "summary";
    }



    /************************************ Controller to handle the Edit page ***************************************************************************/
    @GetMapping("/edit")
    public String getEdit(Model m){
        LocalDate date = LocalDate.now();
        LocalDateTime startTime = LocalDateTime.of(date, LocalTime.MIDNIGHT);
        ArrayList<LocalTime> timeInterval = new ArrayList<>();
        do {
            timeInterval.add(startTime.toLocalTime());
            startTime = startTime.plus(Duration.ofHours(0).plusMinutes(15));
        } while (date.equals(startTime.toLocalDate()));
        m.addAttribute("timeInterval", timeInterval);

        return "edit";
    }


    /************************************ End of Controller to handle the Edit page ***************************************************************************/


    //Checks if the user is logged in and sets the model attributes accordingly per the navbar requirements
    private void loggedInStatusHelper(Model m, Principal p){

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

    //help function to check if the username exist in database
    public boolean checkUserName(String username){
        Iterable<AppUser> allUsers =  userRepository.findAll();
        List<String> allUsername = new ArrayList<>();

        for(AppUser appUser : allUsers){
            allUsername.add(appUser.username);
        }

        if(allUsername.contains(username)){
            return true;
        }else{
            return false;
        }
    }

}
