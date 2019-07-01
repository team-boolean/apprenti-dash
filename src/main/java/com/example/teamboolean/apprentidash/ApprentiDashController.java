package com.example.teamboolean.apprentidash;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import java.time.*;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;


@Controller
public class ApprentiDashController {

    @Autowired
    UserRepository userRepository;

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
    public String startSignUp(@RequestParam (required = false, defaultValue = "")String error,Model m, Principal p){
        //Sets the necessary variables for the nav bar
        timesheetController.loggedInStatusHelper(m, p);
        m.addAttribute("currentPage", "signup");
        m.addAttribute("error",error);
        return "signup";
    }

    @PostMapping("/signup")
    public String addUser(String username, String password, String firstName, String lastName, String managerName){
        try {
            AppUser newUser = new AppUser(username, passwordEncoder.encode(password), firstName, lastName, managerName);
            userRepository.save(newUser);
            Authentication authentication = new UsernamePasswordAuthenticationToken(newUser, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return "redirect:/";
        } catch(DataIntegrityViolationException e){
            return "redirect:/signup?error=uniqueUserName";
        }
    }

    /************************************ End of Controller to handle the Edit page ***************************************************************************/


    //help function to check if the username exist in database
//    public boolean checkUserName(String username){
//        Iterable<AppUser> allUsers =  userRepository.findAll();
//        List<String> allUsername = new ArrayList<>();
//
//        for(AppUser appUser : allUsers){
//            allUsername.add(appUser.username);
//        }
//
//        if(allUsername.contains(username)){
//            return true;
//        }else{
//            return false;
//        }
//    }

}
