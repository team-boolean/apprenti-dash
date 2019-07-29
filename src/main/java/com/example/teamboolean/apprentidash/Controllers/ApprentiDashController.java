package com.example.teamboolean.apprentidash.Controllers;


import com.example.teamboolean.apprentidash.Models.AppUser;
import com.example.teamboolean.apprentidash.Repos.DayRepository;
import com.example.teamboolean.apprentidash.Repos.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;


@Controller
public class ApprentiDashController {

    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    DayRepository dayRepository;

    TimesheetController timesheetController = new TimesheetController();

    //Root route
    @GetMapping("/")
    public RedirectView getRoot(Model m, Principal p){

        // If the user is logged in, redirect them to clock-in
        // otherwise, direct them to home page
        // Huge thanks to David for the idea!
        if(p != null){
            return new RedirectView("/recordHour");
        } else {
            return new RedirectView("/home");
        }
    }

    //Home page
    @GetMapping("/home")
    public String getHome(Model m, Principal p){
        //Sets the necessary variables for the nav bar
        timesheetController.loggedInStatusHelper(m, p);
        m.addAttribute("currentPage", "home");
        return "home";
    }

    //Login Page
    @GetMapping("/login")
    public String getLogin(Model m, Principal p){
        //Sets the necessary variables for the nav bar
        timesheetController.loggedInStatusHelper(m, p);
        m.addAttribute("currentPage", "login");
        return "login";
    }

    //Sign-up page
    @GetMapping("/signup")
    public String startSignUp(Model m, Principal p){
        //Sets the necessary variables for the nav bar
        timesheetController.loggedInStatusHelper(m, p);
        m.addAttribute("currentPage", "signup");
        return "signup";
    }

    //AppUserSettings Page
    @GetMapping("/settings")
    public String getAppUserSettings(Model m, Principal p){
        //Sets the necessary variables for the nav bar
        timesheetController.loggedInStatusHelper(m, p);
        m.addAttribute("currentPage", "appusersettings");
        return "appusersettings";
    }

    //AppUserSettings Page
    @GetMapping("/forum")
    public String getForum(Model m, Principal p){
        //Sets the necessary variables for the nav bar
        timesheetController.loggedInStatusHelper(m, p);
        m.addAttribute("currentPage", "forum");
        return "forum";
    }

    //AppUserSettings Page
    @GetMapping("/thread/{id}")
    public String getThread(@PathVariable String id, Model m, Principal p){
        //Sets the necessary variables for the nav bar
        // add ID Get of Thread form ThreadRepository
        timesheetController.loggedInStatusHelper(m, p);
        m.addAttribute("currentPage", "thread");
        return "thread";
    }


    @PostMapping("/signup")
    public String addUser(String username, String password, String firstName, String lastName, String managerName, String email){
        if (!checkUserName(username)) {
            AppUser newUser = new AppUser(username, passwordEncoder.encode(password), firstName, lastName, managerName, email);
            appUserRepository.save(newUser);
            Authentication authentication = new UsernamePasswordAuthenticationToken(newUser, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return "redirect:/";
        }else {
            return "duplicateUsername";
        }
    }

    /************************************ End of Controller to handle the Edit page ***************************************************************************/


    //help function to check if the username exist in database
    public boolean checkUserName(String username){
        Iterable<AppUser> allUsers =  appUserRepository.findAll();
        List<String> allUsername = new ArrayList<>();

        for(AppUser appUser : allUsers){
            allUsername.add(appUser.getUsername());
        }

        if(allUsername.contains(username)){
            return true;
        }else{
            return false;
        }
    }

}
