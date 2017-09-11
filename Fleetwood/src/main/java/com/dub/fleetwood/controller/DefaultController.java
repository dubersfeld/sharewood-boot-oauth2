package com.dub.fleetwood.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class DefaultController {
	
    @GetMapping({"/", "/backHome", "/index"})
    public String home1() {
    
        return "index";
    }
    
    @GetMapping("/login")
    public String login() {
    
        return "login";
    }
    
    
}