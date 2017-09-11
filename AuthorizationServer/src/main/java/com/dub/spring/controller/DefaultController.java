package com.dub.spring.controller;


import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class DefaultController {
	
	/*
	@Autowired
	SessionRegistry sessionRegistry;
*/

    @GetMapping({"/", "/backHome", "/index"})
    public String home1() {
        return "index";
    }
    
  
    @GetMapping("/login")
    public ModelAndView login(ModelAndView modelAndView) {
    	
    	if(SecurityContextHolder.getContext().getAuthentication() instanceof
                UsernamePasswordAuthenticationToken) {
    		System.out.println("redirect");
    		modelAndView.setViewName("index");
            return modelAndView;
    	}
    	
    	/*
    	System.out.println("number: " +  sessionRegistry.getAllPrincipals().size());
    	
    	modelAndView.addObject("number", sessionRegistry.getAllPrincipals().size());
    	modelAndView.addObject("users", sessionRegistry.getAllPrincipals());  
    */
    	modelAndView.setViewName("login");
    	return modelAndView;
    }
    
    @GetMapping(value="/logout")
    public String logoutPage (HttpServletRequest request, HttpServletResponse response) {
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	// Save current locale before actual logout to enable i18n display
    	Locale locale = LocaleContextHolder.getLocale();
    	
    	if (auth != null) { 
            new SecurityContextLogoutHandler().logout(request, response, auth);
        	auth = SecurityContextHolder.getContext().getAuthentication();
        }

        return "redirect:/login?logout" + "&locale=" + locale.toString();
    }
    

}