package com.example.teamboolean.apprentidash;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

@Controller
public class ApprentiDashController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    DayRepository dayRepository;

    @GetMapping("/")
    public String getHome(){
        return "home";
    }

    @GetMapping("/login")
    public String getLogin(){
        return "login";
    }

    @GetMapping("/signup")
    public String startSignUp(){
        return "signup";
    }

    @PostMapping("/signup")
    public String addUser(String username, String password, String firstName, String lastName){
        AppUser newUser = new AppUser(username, passwordEncoder.encode(password), firstName, lastName);
        userRepository.save(newUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(newUser, null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return "redirect:/";
    }

    //****** The controller methods to handle our Punch In page ******/
    @GetMapping("/recordHour")
    public String recordHour(){
        return "recordHour";
    }

    @PostMapping(value="/recordHour", params="clockIn=clockInValue")
    public ModelAndView clockInSave() {
        ModelAndView modelAndView = new ModelAndView();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now));
        
        return modelAndView;
    }

    @PostMapping(value="/recordHour", params="lunchIn=lunchInValue")
    public ModelAndView lunchInSave() {
        ModelAndView modelAndView = new ModelAndView();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now));
        return modelAndView;
    }

    @PostMapping(value="/recordHour", params="lunchOut=lunchOutValue")
    public ModelAndView lunchOutSave() {
        ModelAndView modelAndView = new ModelAndView();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now));
        return modelAndView;
    }

    @PostMapping(value="/recordHour", params="clockOut=clockOutValue")
    public ModelAndView clockOutSave() {
        ModelAndView modelAndView = new ModelAndView();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now));
        return modelAndView;
    }



}
