package com.example.teamboolean.apprentidash.Controllers;


import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
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
/********************************************************************************/
    //Things we need for sending email to admin.
    static final String FROM = "gaoyasir@gmail.com";

    // Replace recipient@example.com with a "To" address. If your account
    // is still in the sandbox, this address must be verified.
    static final String TO = "gaoyasir@hotmail.com";

    // The subject line for the email.
    static final String SUBJECT = "User login";

    // The HTML body for the email.
    static final String HTMLBODY = "<h1>Welcome, you are in</h1>";

    // The email body for recipients with non-HTML email clients.
    static final String TEXTBODY = "This email was sent to notify you that the user is logedin. ";

    public void sendEmail(){
        try {
            AmazonSimpleEmailService client =
                    AmazonSimpleEmailServiceClientBuilder.standard()
                            // Replace US_WEST_2 with the AWS Region you're using for
                            // Amazon SES.
                            .withRegion(Regions.US_WEST_2).build();
            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(
                            new Destination().withToAddresses(TO))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withHtml(new Content()
                                            .withCharset("UTF-8").withData(HTMLBODY))
                                    .withText(new Content()
                                            .withCharset("UTF-8").withData(TEXTBODY)))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(SUBJECT)))
                    .withSource(FROM);
            client.sendEmail(request);
            System.out.println("Email sent!");
        } catch (Exception ex) {
            System.out.println("The email was not sent. Error message: "
                    + ex.getMessage());
        }
    }


 /********************************************************************************/

 //Root route
    @GetMapping("/")
    public RedirectView getRoot(Model m, Principal p){

        // If the user is logged in, redirect them to clock-in
        // otherwise, direct them to home page
        // Huge thanks to David for the idea!
        if(p != null){
            m.addAttribute("currentPage", "home");
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
        AppUser appUser = appUserRepository.findByUsername(p.getName());
        m.addAttribute("appuser",appUser);
        m.addAttribute("isLoggedIn",true);
        m.addAttribute("userFirstName", appUserRepository.findByUsername(p.getName()).getFirstName());
        m.addAttribute("currentPage", "settings");
        return "appusersettings";
    }

    //AppUserSettings Page
    @PutMapping("/settings")
    public RedirectView editAppUserSettings(Model m, Principal p, String firstname, String lastname, String manager, String email){
        //Sets the necessary variables for the nav bar
        AppUser appUser = appUserRepository.findByUsername(p.getName());
        appUser.setFirstName(firstname);
        appUser.setLastName(lastname);
        appUser.setManagerName(manager);
        appUser.setEmail(email);
        appUserRepository.save(appUser);
        m.addAttribute("isLoggedIn",true);
        m.addAttribute("userFirstName", appUserRepository.findByUsername(p.getName()).getFirstName());
        m.addAttribute("currentPage", "settings");

        return new RedirectView("/");
    }

    @PostMapping("/signup")
    public String addUser(String username, String password, String firstName, String lastName, String managerName, String email){
        if (!checkUserName(username)) {
            AppUser newUser = new AppUser(username, passwordEncoder.encode(password), firstName, lastName, managerName, email);
            appUserRepository.save(newUser);
            Authentication authentication = new UsernamePasswordAuthenticationToken(newUser, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            sendEmail();
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
